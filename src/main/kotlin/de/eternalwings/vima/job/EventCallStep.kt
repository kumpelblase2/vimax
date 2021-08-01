package de.eternalwings.vima.job

import de.eternalwings.vima.event.VideoCreateEvent
import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class EventCallStep(private val videoRepository: VideoRepository, private val pluginManager: PluginManager,
                    private val applicationEventPublisher: ApplicationEventPublisher) {
    fun execute(videoId: Int) {
        val video = videoRepository.getOne(videoId)
        applicationEventPublisher.publishEvent(VideoCreateEvent(this, video))
        if(pluginManager.callEvent(CREATE, video)) {
            videoRepository.save(video)
        }
    }
}
