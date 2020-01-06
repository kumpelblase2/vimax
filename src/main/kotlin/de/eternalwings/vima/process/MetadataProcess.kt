package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.plugin.MetadataInfo
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class MetadataProcess(private val videoRepository: VideoRepository, private val metadataRepository: MetadataRepository) {
    private var cacheEnabled = false
    private val cache: MutableMap<String, MetadataInfo<*>> = hashMapOf()

    fun enableCache(enabled: Boolean) {
        cacheEnabled = enabled
        if (!cacheEnabled) {
            cache.clear()
        }
    }

    fun getSimpleReference(name: String): MetadataInfo<*>? {
        if (cacheEnabled) {
            return cache.computeIfAbsent(name, this::getReadOnlyCopyOf)
        }
        return getReadOnlyCopyOf(name)
    }

    private fun getReadOnlyCopyOf(name: String): MetadataInfo<*> {
        val metadata = metadataRepository.findByName(name) ?: throw IllegalArgumentException()
        return MetadataInfo.fromMetadata(metadata)
    }

    @Transactional
    fun createOrUpdate(metadata: Metadata): Metadata {
        val isNew = metadata.isNew
        if (isNew) {
            metadata.displayOrder = (metadataRepository.getHighestDisplayOrder() ?: 0) + 1
        }

        val saved = metadataRepository.save(metadata)
        if (isNew) {
            videoRepository.addDefaultValueForMetadataIfNotExist(saved.id!!)
        }
        return saved
    }
}
