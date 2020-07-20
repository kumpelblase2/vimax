package de.eternalwings.vima.controller

import de.eternalwings.vima.notification.SSENotificationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class SSEController(private val notificationService: SSENotificationService) {
    @GetMapping("/notifications")
    fun requestNotification(): SseEmitter {
        val emitter = SseEmitter()
        notificationService.addEmitter(emitter)
        return emitter
    }
}
