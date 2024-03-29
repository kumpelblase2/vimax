package de.eternalwings.vima.process

import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.domain.SelectionValue
import de.eternalwings.vima.event.MetadataCreateEvent
import de.eternalwings.vima.event.MetadataDeleteEvent
import de.eternalwings.vima.event.MetadataSelectionOptionRemovedEvent
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityNotFoundException

@Component
class MetadataProcess(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val metadataRepository: MetadataRepository
) {
    @Transactional
    fun createOrUpdate(metadata: Metadata): Metadata {
        val isNew = metadata.isNew
        if (isNew) {
            metadata.displayOrder = (metadataRepository.getHighestDisplayOrder() ?: 0) + 1

            if (metadata.type == SELECTION) {
                val options = metadata.options as SelectionMetadataOptions?
                options?.let {
                    updateSelectionIds(it, emptyList())
                    val currentDefaultValue = it.defaultValue?.name
                    it.defaultValue = it.values.find { selection -> selection.name == currentDefaultValue }
                }
            }
        } else {
            val existing = metadataRepository.getById(metadata.id!!)
            if (existing.type != metadata.type) {
                throw IllegalArgumentException("Changing the type of metadata is currently not possible.")
            }

            if (metadata.type == SELECTION) {
                val options = metadata.options as SelectionMetadataOptions?
                options?.let {
                    val previousValues = (existing.options as SelectionMetadataOptions).values
                    updateSelectionIds(it, previousValues)
                    val removedOptions = findRemovedOptions(it.values, previousValues)
                    if (removedOptions.isNotEmpty()) {
                        removedOptions.forEach { option ->
                            val removeEvent = MetadataSelectionOptionRemovedEvent(this, metadata.id!!, option.id!!)
                            applicationEventPublisher.publishEvent(removeEvent)
                        }
                    }
                }
            }
        }

        val saved = metadataRepository.save(metadata)
        if (isNew) {
            applicationEventPublisher.publishEvent(MetadataCreateEvent(this, saved.id!!))
        }
        return saved
    }

    private fun findRemovedOptions(
        newOptionsList: List<SelectionValue>,
        oldOptionsList: List<SelectionValue>
    ): List<SelectionValue> {
        return oldOptionsList.filter { oldValue -> newOptionsList.none { newValue -> newValue.id == oldValue.id } }
    }

    @Transactional
    fun deleteMetadata(metadataId: Int) {
        val metadata = metadataRepository.findById(metadataId).orElseThrow { EntityNotFoundException("Metadata does not exist") }
        if (metadata.isSystemSpecified) throw IllegalArgumentException("Metadata is system specified metadata.")
        val oldDisplayOrder = metadata.displayOrder
        this.metadataRepository.delete(metadata)
        this.metadataRepository.updateDisplayOrderWithValuesHigher(oldDisplayOrder)
        applicationEventPublisher.publishEvent(MetadataDeleteEvent(this, metadataId))
    }

    fun getAll(): List<Metadata> {
        return metadataRepository.findAll()
    }

    private fun updateSelectionIds(options: SelectionMetadataOptions, previousValues: List<SelectionValue>) {
        var previousHighestId = previousValues.mapNotNull { it.id }.maxOrNull() ?: 0
        for (value in options.values) {
            if (value.id == null) {
                previousHighestId += 1
                value.id = previousHighestId
            }
        }
    }
}
