package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.SmartPlaylist
import de.eternalwings.vima.query.VideoSearcher
import de.eternalwings.vima.repository.SmartPlaylistRepository
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.transaction.Transactional

data class SmartPlaylistCreateInformation(val name: String, val query: String, val orderDirection: Sort.Direction, val orderBy: String)

@RestController
@RequestMapping("/api/smart-playlists")
class SmartPlaylistController(private val smartPlaylistRepository: SmartPlaylistRepository,
                              private val videoSearcher: VideoSearcher) {
    @GetMapping
    fun getAll(): List<SmartPlaylist> {
        return smartPlaylistRepository.findAll()
    }

    @GetMapping("/{id}")
    fun getVideosOf(@PathVariable id: Int): List<Int> {
        val playlist = smartPlaylistRepository.getOne(id)
        return videoSearcher.search(playlist.query!!, playlist.orderBy!!, playlist.orderDirection ?: ASC)
    }

    @PutMapping("/{id}")
    @Transactional
    fun updatePlaylist(@PathVariable id: Int, @RequestBody playlist: SmartPlaylistCreateInformation) : SmartPlaylist {
        val existing = smartPlaylistRepository.getOne(id)
        existing.name = playlist.name
        existing.orderBy = playlist.orderBy
        existing.orderDirection = playlist.orderDirection
        existing.query = playlist.query
        return smartPlaylistRepository.save(existing)
    }

    @PostMapping
    fun createSmartPlaylist(@RequestBody information: SmartPlaylistCreateInformation) : SmartPlaylist {
        val playlist = SmartPlaylist(information.name, information.query, information.orderBy, information.orderDirection)
        return smartPlaylistRepository.save(playlist)
    }
}
