package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Thumbnail
import de.eternalwings.vima.domain.Video
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ThumbnailRepository : JpaRepository<Thumbnail, Int> {
    @Query("SELECT t.location FROM Thumbnail t")
    fun getAllLocations(): List<String>

    fun findByVideo(video: Video): List<Thumbnail>
}
