package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.MetadataValueContainer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetadataContainerRepository : JpaRepository<MetadataValueContainer, Int> {
    fun findByDefinition(metadata: Metadata): List<MetadataValueContainer>
}
