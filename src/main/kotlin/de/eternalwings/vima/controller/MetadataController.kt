package de.eternalwings.vima.controller

import de.eternalwings.vima.MetadataType.BOOLEAN
import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.MetadataType.TAGLIST
import de.eternalwings.vima.MetadataType.TEXT
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.process.MetadataProcess
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.transaction.Transactional
import kotlin.math.max
import kotlin.math.min

@RestController
@RequestMapping("/api")
class MetadataController(private val metadataRepository: MetadataRepository,
                         private val videoRepository: VideoRepository,
                         private val metadataProcess: MetadataProcess) {
    @GetMapping("/metadata")
    fun getAll(): List<Metadata> = metadataRepository.findAll()

    @PostMapping("/metadata")
    @Transactional
    fun createOrUpdateMetadata(@RequestBody metadata: Metadata): Metadata {
        if(metadata.isSystemSpecified) throw IllegalArgumentException("Cannot update system metadata.")
        return metadataProcess.createOrUpdate(metadata)
    }

    @DeleteMapping("/metadata/{id}")
    @Transactional
    fun deleteMetadata(@PathVariable("id") metadataId: Int): Int {
        val metadata = this.metadataRepository.getOne(metadataId)
        if (metadata.isSystemSpecified) throw IllegalArgumentException("Metadata is system specified metadata.")
        this.metadataRepository.delete(metadata)
        this.videoRepository.removeMetadataValueOf(metadataId)
        return metadataId
    }

    @GetMapping("/metadata/{id}/values")
    fun getPossibleValues(@PathVariable("id") metadataId: Int): Set<String> {
        val metadata = metadataRepository.getOne(metadataId)
        return when (metadata.type) {
            BOOLEAN -> setOf("true", "false")
            SELECTION -> (metadata.options as SelectionMetadataOptions).values.mapNotNull { it.name }.toSet()
            TAGLIST -> videoRepository.loadTagValuesFor(metadataId)
            TEXT -> videoRepository.loadStringValuesFor(metadataId)
            else -> emptySet()
        }
    }

    @Transactional
    @PutMapping("/metadata/{id}/insertAt/{pos}")
    fun moveMetadataUp(@PathVariable("id") id: Int, @PathVariable("pos") target: Int): List<Metadata> {
        val metadata = metadataRepository.findAll().sortedBy { it.displayOrder }
        val targetPosition = min(metadata.size - 1, max(0, target))
        val currentIndex = metadata.indexOfFirst { it.id == id }
        if (currentIndex == targetPosition) return emptyList()

        val metadataToMove = metadata[currentIndex]
        val currentTargetOrder = metadata[targetPosition].displayOrder
        val affectedIndices = if (currentIndex < targetPosition) (currentIndex + 1)..targetPosition else targetPosition..(currentIndex - 1)
        val affectedMetadata = metadata.slice(affectedIndices)

        val change = if (currentIndex < targetPosition) -1 else +1
        affectedMetadata.forEach { it.displayOrder += change }
        metadataToMove.displayOrder = currentTargetOrder
        val saved = metadataRepository.saveAll(affectedMetadata)
        val selfSaved = metadataRepository.save(metadataToMove)
        return saved + selfSaved
    }
}
