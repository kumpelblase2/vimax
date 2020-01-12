package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.plugin.MetadataInfo
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class MetadataProcess(private val videoRepository: VideoRepository, private val metadataRepository: MetadataRepository) {

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
        }

        val saved = metadataRepository.save(metadata)
        if (isNew) {
            videoRepository.addMetadataEntryIfNotExists(saved.id!!)
        }
        return saved
    }
}
