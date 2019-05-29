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
    @Query("SELECT DISTINCT v FROM Video v WHERE NOT EXISTS(SELECT mv FROM MetadataValue mv WHERE " +
            "mv.video = v AND mv.metadata.id = ?1 AND " +
            "(mv.booleanValue <> NULL OR mv.dateValue <> NULL OR mv.durationValue <> NULL OR mv.floatingValue <> NULL OR " +
            "mv.numberValue <> NULL OR mv.stringValue <> NULL OR mv.timestampValue <> NULL OR " +
            "(mv.taglistValues <> NULL AND CARDINALITY(mv.taglistValues) > 0) OR mv.timeValue <> NULL OR mv.selectionValue <> NULL))")
    fun findVideosWithMissingMetadata(metadataId: Int, pageable: Pageable): List<Video>
}
