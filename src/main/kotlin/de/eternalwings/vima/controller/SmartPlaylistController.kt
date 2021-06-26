package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.SmartPlaylist
import de.eternalwings.vima.process.CollageCreator
import de.eternalwings.vima.query.VideoSearcher
import de.eternalwings.vima.repository.SmartPlaylistRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class SmartPlaylistCreateInformation(val name: String, val query: String, val orderDirection: Sort.Direction,
                                          val orderBy: String)

@RestController
@RequestMapping("/api/smart-playlists")
class SmartPlaylistController(private val smartPlaylistRepository: SmartPlaylistRepository,
                              private val videoSearcher: VideoSearcher, private val videoRepository: VideoRepository,
                              private val collageCreator: CollageCreator) {
    @Transactional(readOnly = true)
    @GetMapping
    fun getAll(): List<SmartPlaylist> {
        return smartPlaylistRepository.findAll()
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    fun getVideosOf(@PathVariable id: Int): List<Int> {
        val playlist = smartPlaylistRepository.getOne(id)
        return videoSearcher.search(playlist.query!!, playlist.orderBy!!, playlist.orderDirection ?: ASC)
    }

    @Transactional
    @PutMapping("/{id}")
    fun updatePlaylist(@PathVariable id: Int, @RequestBody playlist: SmartPlaylistCreateInformation): SmartPlaylist {
        val existing = smartPlaylistRepository.getOne(id)
        existing.name = playlist.name
        existing.orderBy = playlist.orderBy
        existing.orderDirection = playlist.orderDirection
        existing.query = playlist.query
        return smartPlaylistRepository.save(existing)
    }

    @Transactional
    @PostMapping
    fun createSmartPlaylist(@RequestBody information: SmartPlaylistCreateInformation): SmartPlaylist {
        val playlist = SmartPlaylist(information.name, information.query, information.orderBy, information.orderDirection)
        return smartPlaylistRepository.save(playlist)
    }

    @Transactional
    @DeleteMapping("/{id}")
    fun deletePlaylist(@PathVariable("id") playlistId: Int) {
        smartPlaylistRepository.deleteById(playlistId)
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}/poster", produces = [MediaType.IMAGE_PNG_VALUE])
    fun createPoster(@PathVariable("id") playlistId: Int, @RequestParam("width", defaultValue = "320") width: Int, @RequestParam
    ("height", defaultValue = "180") height: Int):
            ResponseEntity<ByteArray> {
        val playlist = smartPlaylistRepository.getOne(playlistId)
        val videoIds = videoSearcher.search(playlist.query!!)
        if (videoIds.isEmpty()) {
            return ResponseEntity.notFound().build()
        }

        val videoIdsToUse = when (videoIds.size) {
            3 -> (videoIds + videoIds.random()).shuffled()
            2 -> (videoIds + videoIds).shuffled()
            1 -> (listOf(videoIds[0], videoIds[0], videoIds[0], videoIds[0]))
            else -> {
                val randomIndexes = videoIds.indices.shuffled().take(4)
                randomIndexes.map { index -> videoIds[index] }
            }
        }

        val videos = videoRepository.findAllById(videoIdsToUse)
        val thumbnailsToUse = videoIdsToUse.mapNotNull { videos.find { video -> video.id == it } }.map { it.thumbnail!! }

        val resultingImage = collageCreator.createCollageUsing(thumbnailsToUse, width, height)
        return ResponseEntity.ok(resultingImage)
    }
}
