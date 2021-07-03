package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.job.AsyncVideoEventJob
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.MILLIS
import java.util.Collections
import java.util.Date

@Component
class PluginEventDispatcher(
    private val scheduler: Scheduler
) {

    fun dispatchEvent(eventType: EventType, videos: List<Video>, plugins: List<PluginConfig>): Set<Int> {
        val containerVideos = videos.map { VideoContainer.fromVideo(it) }
        plugins.forEach {
            it.runHandlersFor(eventType, containerVideos)
        }

        val changedVideos = mutableSetOf<Int>()
        containerVideos.forEachIndexed { index, container ->
            val original = videos[index]
            var changed = false
            container.changed.forEach { changedMetadata ->
                val metadataValue = container.metadata[changedMetadata] as MetadataValue<Any>
                val originalMetadata = original.metadata!![changedMetadata] as MetadataValue<Any>
                changed = changed || originalMetadata.value != metadataValue.value
                originalMetadata.value = metadataValue.value
            }

            if (changed) {
                changedVideos.add(original.id!!)
            }
        }

        return changedVideos
    }

    fun dispatchEventAsync(eventType: EventType, videos: List<Video>, plugins: List<PluginConfig>) {
        plugins.forEach {
            it.asyncEventHandlers.getOrDefault(eventType, Collections.emptyList()).forEach { handler ->
                videos.forEach { video ->
                    val container = VideoContainer.fromVideo(video)
                    val jobDataMap = JobDataMap(
                        mapOf(
                            "video" to container,
                            "handler" to handler
                        )
                    )
                    val details = JobBuilder.newJob(AsyncVideoEventJob::class.java)
                        .withIdentity("async-events-${it.pluginDescription.name}-${video.id!!}")
                        .usingJobData(jobDataMap)
                        .build()
                    val startTrigger = TriggerBuilder.newTrigger().startNow().build()
                    scheduler.scheduleJob(details, startTrigger)
                }
            }
        }
    }

    private fun PluginConfig.runHandlersFor(eventType: EventType, videos: List<VideoContainer>) {
        val eventHandlersForEvent = this.eventHandlers.getOrDefault(eventType, Collections.emptyList())
        eventHandlersForEvent.forEach { handler ->
            videos.forEach(handler.call)
        }
    }
}
