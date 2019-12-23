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
import org.apache.commons.io.IOUtils
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.util.Collections
import javax.persistence.EntityNotFoundException

@RestController
@RequestMapping("/api")
class VideoController(private val videoRepository: VideoRepository,
                      private val thumbnailRepository: ThumbnailRepository,
                      private val metadataRepository: MetadataRepository,
                      private val videoProcess: VideoProcess,
                      private val pluginManager: PluginManager) {

    private val videoLocationCache: Cache<Int, String> = CacheBuilder.newBuilder().maximumSize(100).build()

    @GetMapping("/videos")
    fun getVideos(@RequestParam("query", required = false, defaultValue = "") query: String,
                  @RequestParam("page", required = false, defaultValue = "0") page: Int,
                  @RequestParam("sortby", required = false, defaultValue = "name") sortProperty: String,
                  @RequestParam("sortdir", required = false) sortDirection: Direction?): List<Video> {
        val videoIds = if (query.isNotBlank()) {
            videoProcess.searchFor(query.trim())
        } else {
            videoRepository.getAllIds()
        }

        val sorting = Sort.by(sortDirection ?: ASC, sortProperty.toLowerCase())
        val paging = PageRequest.of(page, 80, sorting)
        return if (sortProperty == "Name") {
            videoRepository.findVideosSortedByOwnProperty(videoIds, paging)
        } else {
            val metadata = metadataRepository.findByName(sortProperty) ?: throw EntityNotFoundException()
            return when(sortDirection ?: ASC) {
                ASC -> videoRepository.findVideosSortedByAsc(videoIds, metadata.id!!, paging.offset.toInt(), paging.pageSize)
                DESC -> videoRepository.findVideosSortedByDesc(videoIds, metadata.id!!, paging.offset.toInt(), paging.pageSize)
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

    @GetMapping("/video/{id}/thumbnail/{thumb}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getThumbnail(@PathVariable("id") videoId: Int,
                     @PathVariable("thumb") thumbnailId: Int): ByteArray {
        val video = videoRepository.findById(videoId).orElseThrow { EntityNotFoundException() }
        val thumbnail = video.thumbnails.find { it.id == thumbnailId } ?: throw EntityNotFoundException()
        val inputStream = Files.newInputStream(thumbnail.locationPath)
        return IOUtils.toByteArray(inputStream)
    }

    @PutMapping("/video/{id}")
    fun updateVideo(@RequestBody newVideo: Video, @PathVariable("id") id: Int): Video {
        return videoRepository.findById(id).map { oldVideo ->
            oldVideo.selectedThumbnail = newVideo.selectedThumbnail
            oldVideo.name = newVideo.name
            newVideo.metadata?.forEach { metadataValue ->
                if(metadataValue.definition?.isSystemSpecified == false) { // Only update non-system metadata
                    // I can't do the following without those ugly casts. These two values _should_ be of the
                    // same type. And if they aren't we should fail because something is horribly wrong.
                    val existingValue: MetadataValue<Any> = oldVideo.metadata?.find { existing ->
                        existing.definition?.id == metadataValue.definition?.id
                    }?.value as? MetadataValue<Any> ?: return@forEach
                    existingValue.copyFrom(metadataValue.value as MetadataValue<Any>)
                }
            }

            pluginManager.callEvent(UPDATE, oldVideo)

            videoRepository.save(oldVideo)
        }.orElseThrow {
            EntityNotFoundException()
        }
    }

    @PostMapping("/video/{id}/refresh")
    fun refreshThumbnails(@PathVariable("id") videoId: Int): List<Thumbnail> {
        val video = videoRepository.findById(videoId).orElseThrow { EntityNotFoundException() }
        videoProcess.refreshThumbnailsFor(video)
        return thumbnailRepository.findByVideo(video) // Otherwise hibernate cannot load the new thumbnails
    }
}
