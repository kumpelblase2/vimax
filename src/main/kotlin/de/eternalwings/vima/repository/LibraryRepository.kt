package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Library
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LibraryRepository : JpaRepository<Library,Int> {
    fun findByPath(path: String): Library?
}
