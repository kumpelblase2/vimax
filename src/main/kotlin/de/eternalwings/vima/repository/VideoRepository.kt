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
    @Query("SELECT DISTINCT v.id FROM video v WHERE " +
            "NOT EXISTS(SELECT * FROM video_metadata vm " +
            "WHERE vm.video_id = v.id AND vm.definition_id = ?1 AND vm.value IS NOT NULL " +
            "AND json_extract(vm.value, '$.value') IS NOT NULL) ORDER BY v.name",
            nativeQuery = true)
    fun findVideosWithMissingMetadata(metadataId: Int): List<Int>

    @Query("SELECT v.id FROM Video v")
    fun getAllIds() : List<Int>

    @Query("SELECT v FROM Video v WHERE v.id in ?1")
    fun findVideosSortedByOwnProperty(ids: Collection<Int>, pageable: Pageable): List<Video>

    @Query("SELECT v.* FROM video v LEFT JOIN video_metadata vm ON vm.video_id = v.id AND vm.definition_id = ?2 " +
            "WHERE v.id IN ?1 ORDER BY json_extract(vm.value, '$.value') ASC LIMIT ?4 OFFSET ?3",
            nativeQuery = true)
    fun findVideosSortedByAsc(ids: Collection<Int>, metadataId: Int, offset: Int, limit: Int): List<Video>

    @Query("SELECT v.* FROM video v LEFT JOIN video_metadata vm ON vm.video_id = v.id AND vm.definition_id = ?2 " +
            "WHERE v.id IN ?1 ORDER BY json_extract(vm.value, '$.value') DESC LIMIT ?4 OFFSET ?3",
            nativeQuery = true)
    fun findVideosSortedByDesc(ids: Collection<Int>, metadataId: Int, offset: Int, limit: Int): List<Video>
}
