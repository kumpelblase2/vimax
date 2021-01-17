package de.eternalwings.vima.process

import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.domain.SelectionValue
import de.eternalwings.vima.event.VideoUpdateEvent
import de.eternalwings.vima.plugin.MetadataInfo
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@Component
class MetadataProcess(
    private val videoRepository: VideoRepository,
    private val metadataRepository: MetadataRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Cacheable(cacheNames = ["metadata"], key = "#name")
    fun getSimpleReference(name: String): MetadataInfo<*>? {
        return getReadOnlyCopyOf(name)
    }

    private fun getReadOnlyCopyOf(name: String): MetadataInfo<*>? {
        val metadata = metadataRepository.findByName(name) ?: return null
        return MetadataInfo.fromMetadata(metadata)
    }

    @Transactional
    @CacheEvict(cacheNames = ["metadata"], allEntries = true)
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
            val existing = metadataRepository.getOne(metadata.id!!)
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
                            val videos = videoRepository.findVideosWithMetadataValue(metadata.id!!, option.id!!)
                            videoRepository.assignDefaultIfValueForMetadataIs(metadata.id!!, option.id!!)
                            videos.forEach { id ->
                                applicationEventPublisher.publishEvent(VideoUpdateEvent(this, id))
                            }
                        }
                    }
                }
            }
        }

        val saved = metadataRepository.save(metadata)
        if (isNew) {
            videoRepository.addMetadataEntryIfNotExists(saved.id!!)
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
        this.videoRepository.removeMetadataValueOf(metadata.id!!)
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
