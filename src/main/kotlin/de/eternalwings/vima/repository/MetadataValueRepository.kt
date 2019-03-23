package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.MetadataValue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetadataValueRepository : JpaRepository<MetadataValue<*>,Int> {
    fun deleteAllByMetadataId(metadataId: Int)
}
