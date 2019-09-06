package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.domain.Video
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VideoRepository : JpaRepository<Video, Int> {
    fun findAllByOrderByCreationTimeDesc(pageable: Pageable): Page<Video>
    fun findByLocation(location: String): Video?
    fun findByLibrary(library: Library): List<Video>
    @Query("SELECT DISTINCT v.* FROM video v WHERE " +
            "NOT EXISTS(SELECT * FROM video_metadata vm " +
            "WHERE vm.video_id = v.id AND vm.definition_id = ?1 AND vm.value IS NOT NULL " +
            "AND json_extract(vm.value, '$.value') IS NOT NULL)",
            nativeQuery = true)
    fun findVideosWithMissingMetadata(metadataId: Int, pageable: Pageable): List<Video>
}
