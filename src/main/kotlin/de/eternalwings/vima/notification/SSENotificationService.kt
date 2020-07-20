package de.eternalwings.vima.notification

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

@Component
class SSENotificationService {
    val emitters: MutableList<SseEmitter> = CopyOnWriteArrayList()

    fun addEmitter(emitter: SseEmitter) {
        emitters.add(emitter)
        // Send _something_ to the client so it knows it is connected
        emitter.send(SseEmitter.event().data("""{"type":"connected"}"""))
        emitter.onError { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
    }

    fun publish(content: NotificationContent<*>) {
        val emittersToRemove = ArrayList<SseEmitter>()
        emitters.forEach {
            try {
                it.send(SseEmitter.event().data(content, MediaType.APPLICATION_JSON))
            } catch (ex: Exception) {
                LOGGER.debug("Error while trying to send SSE: $ex")
                emittersToRemove.add(it)
            }
        }

        emitters.removeAll(emittersToRemove)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SSENotificationService::class.java)
    }

}
