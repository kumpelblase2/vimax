package de.eternalwings.vima.controller

import de.eternalwings.vima.repository.VideoRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sorting")
class SortModeController(private val videoRepository: VideoRepository) {

    @GetMapping("/{id}")
    fun getVideosWithMissingMetadata(@PathVariable("id") metadataId: Int): List<Int> {
        return videoRepository.findVideosWithMissingMetadata(metadataId)
    }

}
