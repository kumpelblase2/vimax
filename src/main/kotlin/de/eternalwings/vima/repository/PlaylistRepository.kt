package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Playlist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PlaylistRepository : JpaRepository<Playlist, Int> {
    @Query("SELECT MAX(p.position) from VideoInPlaylist p WHERE p.playlist.id = ?1")
    fun getLowestPositionIndexFor(playlistId: Int): Int?

    @Query("DELETE FROM VideoInPlaylist p WHERE p.playlist.id = ?1 AND p.video.id IN ?2")
    fun deleteFromPlaylist(playlistId: Int, videoIds: Collection<Int>)
}
