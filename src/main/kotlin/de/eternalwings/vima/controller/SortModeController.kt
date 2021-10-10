package de.eternalwings.vima.controller

import de.eternalwings.vima.MetadataType.BOOLEAN
import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.domain.BooleanMetadataOptions
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sorting")
class SortModeController(private val videoRepository: VideoRepository, private val metadataRepository: MetadataRepository) {

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    fun getVideosWithMissingMetadata(@PathVariable("id") metadataId: Int): List<Int> {
        val metadata = metadataRepository.getById(metadataId)
        return when (metadata.type) {
            SELECTION -> videoRepository.findVideosWithDefaultMetadataValue(metadata.id!!,
                    (metadata.options as SelectionMetadataOptions).defaultValue!!.name, ".value.name")
            BOOLEAN -> videoRepository.findVideosWithDefaultMetadataValue(metadata.id!!,
                    (metadata.options as BooleanMetadataOptions).defaultValue, ".value")
            else -> videoRepository.findVideosWithMissingMetadata(metadataId)
        }
    }

}
