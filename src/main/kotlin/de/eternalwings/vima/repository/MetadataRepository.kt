package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Metadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MetadataRepository : JpaRepository<Metadata,Int> {
    @Query("SELECT max(m.displayOrder) FROM Metadata m")
    fun getHighestDisplayOrder(): Int?
    fun findByName(name: String): Metadata?

    @Query("SELECT m.id FROM Metadata m")
    fun getAllIds():List<Int>

    @Query("UPDATE Metadata m SET m.displayOrder = m.displayOrder - 1 WHERE m.displayOrder > ?1")
    @Modifying
    fun updateDisplayOrderWithValuesHigher(oldDisplayOrder: Int)
}
