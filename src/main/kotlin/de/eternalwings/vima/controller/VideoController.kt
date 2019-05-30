package de.eternalwings.vima.controller

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Thumbnail
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.process.VideoProcess
import de.eternalwings.vima.repository.ThumbnailRepository
import de.eternalwings.vima.repository.VideoRepository
import org.apache.commons.io.IOUtils
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageRequest
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
                      private val videoProcess: VideoProcess) {

    private val videoLocationCache: Cache<Int, String> = CacheBuilder.newBuilder().maximumSize(100).build()

    @GetMapping("/videos")
    fun getAllVideos(@RequestParam("query", required = false) query: String?): List<Video> {
        return if (query != null && query.isNotBlank()) {
            videoProcess.searchFor(query.trim())
        } else {
            videoRepository.findAll()
        }
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
    fun thumbnails(@PathVariable("id") id: Int): List<Long> {
        return videoRepository.findById(id).map { it.thumbnails }.orElse(Collections.emptyList())
            .mapNotNull { it.id }
    }

    @GetMapping("/video/{id}/thumbnail/{thumb}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getThumbnail(@PathVariable("id") videoId: Int,
                     @PathVariable("thumb") thumbnailId: Long): ByteArray {
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
            newVideo.metadata.forEach { metadataValue ->
                // I can't do the following without those ugly casts. These two values _should_ be of the
                // same type. And if they aren't we should fail because something is horribly wrong.
                val existingValue = oldVideo.metadata.find { existing ->
                    existing.id == metadataValue.id
                } as MetadataValue<Any>?
                existingValue?.copyFrom(metadataValue as MetadataValue<Any>)
            }
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
