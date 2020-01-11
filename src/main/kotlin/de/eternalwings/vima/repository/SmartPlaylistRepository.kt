package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.SmartPlaylist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SmartPlaylistRepository : JpaRepository<SmartPlaylist, Int> {
}
