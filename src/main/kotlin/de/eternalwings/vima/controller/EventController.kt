package de.eternalwings.vima.controller

import de.eternalwings.vima.plugin.EventType
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/event")
class EventController(private val videoRepository: VideoRepository) {
    @PostMapping
    fun call(
            @RequestParam(required = true, name = "type") eventType: EventType,
            @RequestParam(required = true, name = "video") videoId: Int
    ) {
        val video = videoRepository.getOne(videoId)
        PluginManager.callEvent(eventType, video)
        videoRepository.save(video)
    }
}
