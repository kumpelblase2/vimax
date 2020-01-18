package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.domain.Video
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface VideoRepository : JpaRepository<Video, Int> {
    fun findAllByOrderByCreationTimeDesc(pageable: Pageable): Page<Video>
    fun findByLocation(location: String): Video?
    fun findByLibrary(library: Library): List<Video>
    @Query("SELECT DISTINCT v.id FROM video v WHERE json_extract(v.metadata_values, '$.' || ?1 || '.value') IS NULL ORDER BY v.name",
            nativeQuery = true)
    fun findVideosWithMissingMetadata(metadataId: Int): List<Int>

    @Query("SELECT v.id FROM Video v")
    fun getAllIds(): List<Int>

    @Query("SELECT v.id FROM Video v WHERE v.id in ?1")
    fun findVideoIdsSortedByOwnProperty(ids: Collection<Int>, pageable: Pageable): List<Int>

    @Query("SELECT v.id FROM video v WHERE v.id IN ?1 ORDER BY json_extract(v.metadata_values, '$.' || ?2 || '.value') ASC LIMIT ?4 OFFSET ?3",
            nativeQuery = true)
    fun findVideoIdsSortedByAsc(ids: Collection<Int>, metadataId: Int, offset: Int, limit: Int): List<Int>

    @Query("SELECT v.id FROM video v WHERE v.id IN ?1 ORDER BY json_extract(v.metadata_values, '$.' || ?2 || '.value') DESC LIMIT ?4 OFFSET ?3",
            nativeQuery = true)
    fun findVideoIdsSortedByDesc(ids: Collection<Int>, metadataId: Int, offset: Int, limit: Int): List<Int>

    fun findVideosByUpdateTimeAfter(timestamp: LocalDateTime): List<Video>

    fun findVideoByLocation(location: String): Video?

    @Query("SELECT DISTINCT json_extract(v.metadata_values, '$.' || ?1 || '.value') AS value FROM video v WHERE value IS NOT NULL AND" +
            " value <> '' ORDER BY value",
            nativeQuery = true)
    fun loadStringValuesFor(metadataId: Int): MutableSet<String>

    @Query("SELECT DISTINCT json_each.value AS value FROM video v, json_each(v.metadata_values, '$.' || ?1 || '.value') " +
            "WHERE value IS NOT NULL ORDER BY value",
            nativeQuery = true)
    fun loadTagValuesFor(metadataId: Int): MutableSet<String>

    @Query("SELECT DISTINCT v.id FROM video v WHERE json_extract(v.metadata_values, '$.' || ?1 || ?3) = ?2",
            nativeQuery = true)
    fun findVideosWithMetadataValue(metadataId: Int, metadataValue: Any?, path: String): List<Int>

    @Modifying
    @Query("UPDATE video SET metadata_values = json_insert(metadata_values, '$.' || ?1, json_object('meta-type', json_extract(" +
            "(SELECT m.options FROM metadata m WHERE m.id = ?1), '$.type'), 'value', NULL))",
            nativeQuery = true)
    fun addMetadataEntryIfNotExists(metadataId: Int)

    @Modifying
    @Query("UPDATE video SET metadata_values = json_remove(metadata_values, '$.' || ?1)", nativeQuery = true)
    fun removeMetadataValueOf(metadataId: Int)
}
