package de.eternalwings.vima.controller

import de.eternalwings.vima.controller.PlaylistInformation.Companion
import de.eternalwings.vima.domain.Playlist
import de.eternalwings.vima.repository.PlaylistRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

data class PlaylistInformation(val name: String, val videoIds: List<Int>) {
    companion object {
        fun from(playlist: Playlist): PlaylistInformation {
            return PlaylistInformation(playlist.name!!, playlist.videos.map { it.id!! })
        }
    }
}

@RestController
@RequestMapping("/api/playlists")
class PlaylistController(private val playlistRepository: PlaylistRepository, private val videoRepository: VideoRepository) {
    @GetMapping
    fun getAll(): List<PlaylistInformation> {
        return playlistRepository.findAll().map { PlaylistInformation.from(it) }
    }

    @PostMapping
    fun createPlaylist(@RequestBody playlist: PlaylistInformation): PlaylistInformation {
        val newPlaylist = Playlist(playlist.name, videoRepository.findAllById(playlist.videoIds))
        return PlaylistInformation.from(playlistRepository.save(newPlaylist))
    }

    @Transactional
    @PutMapping("/{id}/add")
    fun addVideoToPlaylist(@PathVariable("id") playlistId: Int, @RequestBody videos: List<Int>): PlaylistInformation {
        val playlist = playlistRepository.findById(playlistId).orElseThrow { EntityNotFoundException() }
        val videosToAdd = videos.filter { playlist.videos.none { video -> video.id == it } }
        playlist.videos.addAll(videoRepository.findAllById(videosToAdd))
        return Companion.from(playlistRepository.save(playlist))
    }

    @Transactional
    @PutMapping("/{id}/remove")
    fun removeVideoFromPlaylist(@PathVariable("id") playlistId: Int, @RequestBody videos: List<Int>): PlaylistInformation {
        val playlist = playlistRepository.findById(playlistId).orElseThrow { EntityNotFoundException() }
        playlist.videos.removeIf { videos.contains(it.id) }
        return Companion.from(playlistRepository.save(playlist))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") playlistId: Int) {
        playlistRepository.deleteById(playlistId)
    }
}
