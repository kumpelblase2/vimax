package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video
import java.util.Collections

data class PluginConfig(val pluginName: String, private val eventHandlers: Map<EventType, Collection<VideoHandler>>,
                        private val allMetadata: List<MetadataContainer<*>>) {

    fun callHandlerFor(eventType: EventType, video: Video) {
        callHandlerFor(eventType, listOf(video))
    }

    fun callHandlerFor(eventType: EventType, videos: List<Video>) {
        val containerVideos = videos.map { VideoContainer.fromVideo(it) }
        eventHandlers.getOrDefault(eventType, Collections.emptyList()).forEach { handler ->
            containerVideos.forEach { handler(it) }
        }

        containerVideos.forEachIndexed { index, container ->
            val original = videos[index]
            container.changed.forEach { changedMetadata ->
                val metadataValue = container.metadata[changedMetadata] as MetadataValue<Any>
                val originalMetadata = original.metadata!![changedMetadata] as MetadataValue<Any>
                originalMetadata.value = metadataValue.value
            }
        }
    }

    fun hasHandlerFor(eventType: EventType): Boolean {
        return eventHandlers.getOrDefault(eventType, Collections.emptyList()).isNotEmpty()
    }

    companion object {
        fun fromCreationContext(pluginCreateContext: PluginCreateContext): PluginConfig {
            return PluginConfig(pluginCreateContext.name, pluginCreateContext.eventHandlers, pluginCreateContext.ownedMetadata)
        }
    }
}
