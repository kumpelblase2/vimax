package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.EventType
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/event")
class EventController(private val videoRepository: VideoRepository, private val pluginManager: PluginManager) {
    @PostMapping
    fun call(
            @RequestParam(required = true, name = "type") eventType: EventType,
            @RequestParam(required = true, name = "video") videoId: Int
    ): ResponseEntity<Video> {
        val video = videoRepository.getOne(videoId)
        val updated = pluginManager.callEvent(eventType, video)
        val result = videoRepository.save(video)
        return if(updated) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity(HttpStatus.NOT_MODIFIED)
        }
    }
}
