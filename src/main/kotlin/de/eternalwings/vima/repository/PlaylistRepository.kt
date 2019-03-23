package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.Playlist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaylistRepository : JpaRepository<Playlist, Int>
