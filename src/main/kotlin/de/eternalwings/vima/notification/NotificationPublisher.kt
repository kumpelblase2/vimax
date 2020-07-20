package de.eternalwings.vima.notification

import de.eternalwings.vima.event.VideoCreateEvent
import de.eternalwings.vima.event.VideoDeleteEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class NotificationPublisher(private val notificationService: SSENotificationService) {

    private data class VideoDeleteContent(val videoId: Int)

    @EventListener
    fun onVideoDelete(event: VideoDeleteEvent) {
        notificationService.publish(NotificationContent("video-delete", VideoDeleteContent(event.video.id!!)))
    }

    @EventListener
    fun onVideoCreate(event: VideoCreateEvent) {
        notificationService.publish(NotificationContent("video-create", event.video))
    }
}
