package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.SelectionValues
import de.eternalwings.vima.process.VideoMetadataUpdater
import de.eternalwings.vima.repository.MetadataContainerRepository
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
                         private val metadataValueRepository: MetadataContainerRepository,
                         private val videoMetadataUpdater: VideoMetadataUpdater) {
    @GetMapping("/metadata")
    fun getAll(): List<Metadata> = metadataRepository.findAll()

    @PostMapping("/metadata")
    @Transactional
    fun createOrUpdateMetadata(@RequestBody metadata: Metadata): Metadata {
        val isNew = metadata.isNew
        if (isNew) {
            val existingSequenceNumber = metadataRepository.getHighestDisplayOrder() ?: 0
            metadata.displayOrder = existingSequenceNumber + 1
        }

        val savedMetadata = metadataRepository.save(metadata)
        if (isNew) {
            videoMetadataUpdater.addMetadata(savedMetadata)
        }
        return savedMetadata
    }

    @DeleteMapping("/metadata/{id}")
    @Transactional
    fun deleteMetadata(@PathVariable("id") metadataId: Int): Int {
        val metadata = this.metadataRepository.getOne(metadataId)
        this.metadataRepository.delete(metadata)
        return metadataId
    }

    @GetMapping("/metadata/{id}/values")
    fun getPossibleValues(@PathVariable("id") metadataId: Int): List<Any> {
        val values = metadataValueRepository.findByDefinition(metadataRepository.getOne(metadataId)).asSequence()
            .mapNotNull { it.value }.filter { it.value != null }.mapNotNull { it.value }
        return values.flatMap {
            when (it) {
                is List<*> -> it.asSequence() as Sequence<Any>
                is SelectionValues -> sequenceOf(it.name!!)
                else -> sequenceOf(it)
            }
        }.filterNotNull().distinct().toList()
    }
}
