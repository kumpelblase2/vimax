package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Metadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MetadataRepository : JpaRepository<Metadata,Int> {
    @Query("SELECT max(m.displayOrder) FROM Metadata m")
    fun getHighestDisplayOrder(): Int?
    fun findByName(name: String): Metadata?
}
