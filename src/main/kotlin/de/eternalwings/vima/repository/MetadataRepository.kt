package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Metadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetadataRepository : JpaRepository<Metadata,Int>
