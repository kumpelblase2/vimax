package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.domain.Video
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VideoRepository : JpaRepository<Video, Int> {
    fun findAllByOrderByCreationTimeDesc(pageable: Pageable): Page<Video>
    fun findByLocation(location: String): Video?
    fun findByLibrary(library: Library): List<Video>
}
