package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Thumbnail
import de.eternalwings.vima.domain.Video
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ThumbnailRepository : JpaRepository<Thumbnail, Int> {
    @Modifying
    @Query("INSERT INTO thumbnail(location, video_id) VALUES(?1, ?2)", nativeQuery = true)
    @Transactional
    fun insertThumbnailForVideo(location: String, videoId: Int)

    fun findByVideo(video: Video): List<Thumbnail>
}
