package de.eternalwings.vima.controller

import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.process.VideoMetadataUpdater
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.transaction.Transactional

@RestController
@RequestMapping("/api")
class MetadataController(private val metadataRepository: MetadataRepository,
                         private val videoMetadataUpdater: VideoMetadataUpdater) {
    @GetMapping("/metadata")
    fun getAll(): List<Metadata> = metadataRepository.findAll()

    @PostMapping("/metadata")
    @Transactional
    fun createOrUpdateMetadata(@RequestBody metadata: Metadata): Metadata {
        val savedMetadata: Metadata
        if (metadata.type == SELECTION && (metadata.options as SelectionMetadataOptions).defaultSelectValue != null) {
            // This is because we need an existing reference when saving the default value so it points to
            // the same selection option
            val oldDefaultValue = (metadata.options as SelectionMetadataOptions).defaultSelectValue
            (metadata.options as SelectionMetadataOptions).defaultSelectValue = null
            val tempSaved = metadataRepository.save(metadata)
            (tempSaved.options as SelectionMetadataOptions).defaultSelectValue =
                    (tempSaved.options as SelectionMetadataOptions).values.find {
                        it.name == oldDefaultValue?.name
                    }
            savedMetadata = this.metadataRepository.save(tempSaved)
        } else {
            savedMetadata = this.metadataRepository.save(metadata)
        }
        videoMetadataUpdater.addMetadata(savedMetadata)
        return savedMetadata
    }

    @DeleteMapping("/metadata/{id}")
    @Transactional
    fun deleteMetadata(@PathVariable("id") metadataId: Int): Int {
        val metadata = this.metadataRepository.getOne(metadataId)
        videoMetadataUpdater.deleteMetadata(metadata)
        this.metadataRepository.delete(metadata)
        return metadataId
    }
}
