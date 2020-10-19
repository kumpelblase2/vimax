package de.eternalwings.vima.controller

import de.eternalwings.vima.controller.PlaylistInformation.Companion
import de.eternalwings.vima.domain.Playlist
import de.eternalwings.vima.process.CollageCreator
import de.eternalwings.vima.repository.PlaylistRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

data class PlaylistCreateInformation(val name: String, val videoIds: List<Int>)

data class PlaylistInformation(val id: Int, val name: String, val videoIds: List<Int>) {
    companion object {
        fun from(playlist: Playlist): PlaylistInformation {
            val videoIds = ArrayList(playlist.videos).sortedBy { it.position }.mapNotNull { it.video?.id }
            return PlaylistInformation(playlist.id!!, playlist.name!!, videoIds)
        }
    }
}

@RestController
@RequestMapping("/api/playlists")
class PlaylistController(private val playlistRepository: PlaylistRepository, private val videoRepository: VideoRepository,
                         private val collageCreator: CollageCreator) {
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
    @PutMapping("/{id}")
    fun updatePlaylistName(@PathVariable("id") playlistId: Int, @RequestBody newName: String): PlaylistInformation {
        val playlist = playlistRepository.getOne(playlistId)
        playlist.name = newName
        val saved = playlistRepository.save(playlist)
        return Companion.from(saved)
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
        return Companion.from(playlistRepository.getOne(playlistId))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") playlistId: Int) {
        playlistRepository.deleteById(playlistId)
    }

    @Transactional
    @PutMapping("/{id}/order")
    fun updatePlaylistOrder(@PathVariable("id") playlistId: Int, @RequestBody videos: List<Int>): PlaylistInformation {
        val playlist = playlistRepository.findById(playlistId).orElseThrow { EntityNotFoundException() }
        playlist.videos.forEach { playlistPosition ->
            playlistPosition.position = videos.indexOf(playlistPosition.video?.id)
        }
        return Companion.from(playlistRepository.save(playlist))
    }

    @GetMapping("/{id}/poster", produces = [MediaType.IMAGE_PNG_VALUE])
    fun createPoster(@PathVariable("id") playlistId: Int, @RequestParam("width", defaultValue = "320") width: Int, @RequestParam
    ("height", defaultValue = "180") height: Int):
            ResponseEntity<ByteArray> {
        val playlist = playlistRepository.getOne(playlistId)
        val videosThumbnails = playlist.videos.mapNotNull { vip -> vip.video?.thumbnail }
        if (videosThumbnails.isEmpty()) {
            return ResponseEntity.notFound().build()
        }

        val thumbnailsToUse = when (videosThumbnails.size) {
            3 -> (videosThumbnails + videosThumbnails.random()).shuffled()
            2 -> (videosThumbnails + videosThumbnails).shuffled()
            1 -> (listOf(videosThumbnails[0], videosThumbnails[0], videosThumbnails[0], videosThumbnails[0]))
            else -> {
                val randomIndexes = videosThumbnails.indices.shuffled().take(4)
                randomIndexes.map { index -> videosThumbnails[index] }
            }
        }

        val resultingImage = collageCreator.createCollageUsing(thumbnailsToUse, width, height)
        return ResponseEntity.ok(resultingImage)
    }
}
