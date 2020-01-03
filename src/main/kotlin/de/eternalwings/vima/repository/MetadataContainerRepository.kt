package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.MetadataValueContainer
import de.eternalwings.vima.domain.VideoMetadataCount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MetadataContainerRepository : JpaRepository<MetadataValueContainer, Int> {
    @Query("SELECT DISTINCT json_extract(container.value, '$.value') AS somenewvalue FROM video_metadata container WHERE " +
            "container.definition_id = ?1 AND someNewValue IS NOT NULL", nativeQuery = true)
    fun getStringValuesFor(metadataId: Int): MutableSet<String>

    @Query("SELECT DISTINCT json_each.value AS somenewvalue FROM video_metadata container, json_each(container.value, '$.value') " +
            "WHERE container.definition_id = ?1 AND someNewValue IS NOT NULL", nativeQuery = true)
    fun getTagValuesFor(metadataId: Int): MutableSet<String>

    @Query("SELECT new de.eternalwings.vima.domain.VideoMetadataCount(mvc.video.id, COUNT(mvc)) FROM " +
            "MetadataValueContainer mvc GROUP BY mvc.video.id")
    fun getMetadataCountsForVideos(): List<VideoMetadataCount>
}
