package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/sorting")
class SortModeController(private val videoRepository: VideoRepository) {

    @GetMapping("/{id}")
    fun getVideosWithMissingMetadata(@PathVariable("id") metadataId: Int,
                                     @RequestParam("limit", required = false, defaultValue = "10") limit: Int,
                                     @RequestParam("page", required = false, defaultValue = "0") page: Int): List<Video> {

        return videoRepository.findVideosWithMissingMetadata(metadataId, PageRequest.of(page, limit))
    }

}
