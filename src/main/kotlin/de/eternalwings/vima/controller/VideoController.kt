package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.TaglistMetadataValue
import de.eternalwings.vima.domain.Thumbnail
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.process.VideoProcess
import de.eternalwings.vima.query.VideoSearcher
import de.eternalwings.vima.repository.ThumbnailRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Collections
import java.util.concurrent.TimeUnit.DAYS
import javax.persistence.EntityNotFoundException

data class VideoFileInformation(val location: String, val timestamp: LocalDateTime)
data class MultiVideoChangeInformation(val videoIds: List<Int>, val newValues: Map<Int, MetadataValue<*>>)

@RestController
@RequestMapping("/api")
class VideoController(private val videoRepository: VideoRepository,
                      private val thumbnailRepository: ThumbnailRepository,
                      private val videoProcess: VideoProcess,
                      private val videoSearcher: VideoSearcher,
                      transactionManager: PlatformTransactionManager) {

    private val transactionTemplate = TransactionTemplate(transactionManager)

    @GetMapping("/videos")
    fun getVideos(@RequestParam("query", required = false, defaultValue = "") query: String,
                  @RequestParam("sortby", required = false, defaultValue = "name") sortProperty: String,
                  @RequestParam("sortdir", required = false) sortDirection: Direction?): List<Int> {
        return videoSearcher.search(query, sortProperty, sortDirection ?: ASC)
    }

    @GetMapping("/videos/byid")
    fun getVideosById(@RequestParam("ids", required = true) ids: List<Int>): List<Video> {
        return videoRepository.findAllById(ids)
    }

    @GetMapping("/home")
    fun getRecentVideos(): List<Video> {
        return videoRepository.findAllByOrderByCreationTimeDesc(PageRequest.of(0, 10)).content
    }

    @GetMapping("/video/{id}")
    fun getVideo(@PathVariable("id") id: Int) = videoRepository.getOne(id)

    @GetMapping("/video/{id}/stream")
    @Cacheable(cacheNames = ["streams"], key = "#id")
    fun streamVideo(@PathVariable("id") id: Int): ResponseEntity<Resource> {
        val information = transactionTemplate.execute {
            val video = videoRepository.findById(id).orElseThrow { EntityNotFoundException() }
            VideoFileInformation(video.location!!, video.creationTime!!)
        }!!
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(365, DAYS))
            .lastModified(information.timestamp.toInstant(ZoneOffset.UTC))
            .body(FileSystemResource(information.location))
    }

    @GetMapping("/video/{id}/thumbnails")
    fun thumbnails(@PathVariable("id") id: Int): List<Int> {
        return videoRepository.findById(id).map { it.thumbnails }.orElse(Collections.emptyList())
            .mapNotNull { it.id }
    }

    @GetMapping("/video/thumbnail/{thumb}")
    @Cacheable(cacheNames = ["thumbnails"], key = "#thumbnailId")
    fun getThumbnail(@PathVariable("thumb") thumbnailId: Int): ResponseEntity<Resource> {
        val location = transactionTemplate.execute {
            val thumbnail = thumbnailRepository.findById(thumbnailId).orElseThrow { EntityNotFoundException() }
            thumbnail.location
        }!!

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(365, DAYS))
            .lastModified(Instant.from(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)))
            .body(FileSystemResource(location))
    }

    @Transactional
    @PutMapping("/video/{id}")
    fun updateVideo(@RequestBody newVideo: Video, @PathVariable("id") id: Int): Video {
        return videoProcess.updateVideoFromUser(newVideo)
    }

    @Transactional
    @PutMapping("/videos")
    fun updateVideos(@RequestBody changeData: MultiVideoChangeInformation): List<Video> {
        val videos = videoRepository.findAllById(changeData.videoIds)
        changeData.newValues.forEach { (id, value) ->
            if(value is TaglistMetadataValue) {
                val tags = value.value ?: emptySet()
                videos.forEach { video ->
                    val currentValues = video.metadata!![id] as TaglistMetadataValue
                    tags.forEach { tag ->
                        val tagName = tag.substring(1)
                        val changeType = tag[0]
                        currentValues.value = when (changeType) {
                            '-' -> (currentValues.value ?: emptySet()) - setOf(tagName)
                            '+' -> (currentValues.value ?: emptySet()) + setOf(tagName)
                            else -> throw IllegalArgumentException("Tag $tag was wrongly formatter for update.")
                        }
                    }
                }
            } else {
                videos.forEach { video ->
                    video.metadata!![id] = value
                }
            }
        }
        return videoProcess.updateVideosFromUser(videos)
    }

    @PostMapping("/video/{id}/refresh")
    fun refreshThumbnails(@PathVariable("id") videoId: Int): List<Thumbnail> {
        val video = videoRepository.findById(videoId).orElseThrow { EntityNotFoundException() }
        videoProcess.refreshThumbnailsFor(video)
        return thumbnailRepository.findByVideo(video) // Otherwise hibernate cannot load the new thumbnails
    }
}
