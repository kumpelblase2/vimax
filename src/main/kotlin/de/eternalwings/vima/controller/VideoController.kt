package de.eternalwings.vima.controller

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Thumbnail
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.EventType.UPDATE
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.process.VideoProcess
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.ThumbnailRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Collections
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@RestController
@RequestMapping("/api")
class VideoController(private val videoRepository: VideoRepository,
                      private val thumbnailRepository: ThumbnailRepository,
                      private val metadataRepository: MetadataRepository,
                      private val videoProcess: VideoProcess,
                      private val pluginManager: PluginManager) {

    private val videoLocationCache: Cache<Int, String> = CacheBuilder.newBuilder().maximumSize(100).build()
    private val thumbnailLocationCache: Cache<Int, String> = CacheBuilder.newBuilder().maximumSize(100).build()

    @GetMapping("/videos")
    fun getVideos(@RequestParam("query", required = false, defaultValue = "") query: String,
                  @RequestParam("page", required = false, defaultValue = "0") page: Int,
                  @RequestParam("sortby", required = false, defaultValue = "name") sortProperty: String,
                  @RequestParam("sortdir", required = false) sortDirection: Direction?): List<Int> {
        val videoIds = if (query.isNotBlank()) {
            videoProcess.searchFor(query.trim())
        } else {
            videoRepository.getAllIds()
        }

        val sorting = Sort.by(sortDirection ?: ASC, sortProperty.toLowerCase())
        val paging = PageRequest.of(page, 80, sorting)
        return if (sortProperty == "Name") {
            videoRepository.findVideoIdsSortedByOwnProperty(videoIds, paging)
        } else {
            val metadata = metadataRepository.findByName(sortProperty) ?: throw EntityNotFoundException()
            return when (sortDirection ?: ASC) {
                ASC -> videoRepository.findVideoIdsSortedByAsc(videoIds, metadata.id!!, paging.offset.toInt(), paging.pageSize)
                DESC -> videoRepository.findVideoIdsSortedByDesc(videoIds, metadata.id!!, paging.offset.toInt(), paging.pageSize)
            }
        }
    }

    @GetMapping("/videos/byid")
    fun getVideosToSortById(@RequestParam("ids", required = true) ids: List<Int>): List<Video> {
        return videoRepository.findAllById(ids)
    }

    @GetMapping("/home")
    fun getRecentVideos(): List<Video> {
        return videoRepository.findAllByOrderByCreationTimeDesc(PageRequest.of(0, 10)).content
    }

    @GetMapping("/video/{id}")
    fun getVideo(@PathVariable("id") id: Int) = videoRepository.getOne(id)

    @GetMapping("/video/{id}/stream")
    fun streamVideo(@PathVariable("id") id: Int): ResponseEntity<Resource> {
        val location = videoLocationCache.get(id) {
            val video = videoRepository.findById(id).orElseThrow { EntityNotFoundException() }
            video.location!!
        }
        return ResponseEntity.status(OK).body(FileSystemResource(location))
    }

    @GetMapping("/video/{id}/thumbnails")
    fun thumbnails(@PathVariable("id") id: Int): List<Int> {
        return videoRepository.findById(id).map { it.thumbnails }.orElse(Collections.emptyList())
            .mapNotNull { it.id }
    }

    @GetMapping("/video/thumbnail/{thumb}")
    fun getThumbnail(@PathVariable("thumb") thumbnailId: Int): ResponseEntity<Resource> {
        val location = thumbnailLocationCache.get(thumbnailId) {
            val thumbnail = thumbnailRepository.findById(thumbnailId).orElseThrow { EntityNotFoundException() }
            thumbnail.location!!
        }
        return ResponseEntity.ok(FileSystemResource(location))
    }

    @Transactional
    @PutMapping("/video/{id}")
    fun updateVideo(@RequestBody newVideo: Video, @PathVariable("id") id: Int): Video {
        val existing = videoRepository.findById(id).orElseThrow { EntityNotFoundException() }
        val metadata = metadataRepository.findAll()
        existing.selectedThumbnail = newVideo.selectedThumbnail
        existing.name = newVideo.name
        newVideo.metadata?.forEach { metadataId, value ->
            val definition = metadata.find { it.id == metadataId } ?: return@forEach
            if (!definition.isSystemSpecified) { // Only update non-system metadata
                val existingValue = existing.metadata?.get(metadataId) ?: throw IllegalStateException("Missing metadata")
                @Suppress("UNCHECKED_CAST") // Can't do it without the cast :(
                (existingValue as MetadataValue<Any>).value = value.value
            }
        }

        existing.metadata!!.entries.removeIf { entry -> metadata.none { it.id == entry.key } }

        pluginManager.callEvent(UPDATE, existing)

        return videoRepository.save(existing)
    }

    @PostMapping("/video/{id}/refresh")
    fun refreshThumbnails(@PathVariable("id") videoId: Int): List<Thumbnail> {
        val video = videoRepository.findById(videoId).orElseThrow { EntityNotFoundException() }
        videoProcess.refreshThumbnailsFor(video)
        return thumbnailRepository.findByVideo(video) // Otherwise hibernate cannot load the new thumbnails
    }
}
