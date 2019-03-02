package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.Video
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.util.Collections
import javax.persistence.EntityNotFoundException

@RestController
@RequestMapping("/api")
class VideoController(val videoRepository: VideoRepository) {
    @GetMapping("/videos")
    fun getAllVideos(): List<Video> {
        return videoRepository.findAll()
    }

    @GetMapping("/home")
    fun getRecentVideos(): List<Video> {
        return videoRepository.findAllByOrderByCreationTimeDesc(PageRequest.of(0, 10)).content
    }

    @GetMapping("/video/{id}/stream")
    fun streamVideo(@PathVariable("id") id: Int): ResponseEntity<Resource> {
        val video = videoRepository.findById(id).orElseThrow { EntityNotFoundException() }
        return ResponseEntity.status(OK).body(FileSystemResource(video.location!!))
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
}
