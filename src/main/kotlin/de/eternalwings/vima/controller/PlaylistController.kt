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

data class PlaylistCreateInformation(val name: String, val videoIds: List<Int>)

data class PlaylistInformation(val id: Int, val name: String, val videoIds: List<Int>) {
    companion object {
        fun from(playlist: Playlist): PlaylistInformation {
            return PlaylistInformation(playlist.id!!, playlist.name!!, playlist.videos.map { it.video!!.id!! })
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
    fun createPlaylist(@RequestBody playlist: PlaylistCreateInformation): PlaylistInformation {
        val newPlaylist = Playlist(playlist.name)
        newPlaylist.addVideos(videoRepository.findAllById(playlist.videoIds))
        return PlaylistInformation.from(playlistRepository.save(newPlaylist))
    }

    @Transactional
    @PutMapping("/{id}/add")
    fun addVideoToPlaylist(@PathVariable("id") playlistId: Int, @RequestBody videos: List<Int>): PlaylistInformation {
        val playlist = playlistRepository.findById(playlistId).orElseThrow { EntityNotFoundException() }
        val videosToAdd = videos.filter { playlist.videos.none { video -> video.video?.id == it } }
        val lowestPositionBefore = playlistRepository.getLowestPositionIndexFor(playlist.id!!) ?: 0
        playlist.addVideos(videoRepository.findAllById(videosToAdd), lowestPositionBefore + 1)
        return Companion.from(playlistRepository.save(playlist))
    }

    @Transactional
    @PutMapping("/{id}/remove")
    fun removeVideoFromPlaylist(@PathVariable("id") playlistId: Int, @RequestBody videos: List<Int>): PlaylistInformation {
        playlistRepository.deleteFromPlaylist(playlistId, videos)
        return Companion.from(playlistRepository.save(playlistRepository.getOne(playlistId)))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") playlistId: Int) {
        playlistRepository.deleteById(playlistId)
    }
}
