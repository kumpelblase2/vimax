package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.api.MetadataRef
import de.eternalwings.vima.plugin.api.PluginConfigurationEnvironment
import java.util.Collections

data class PluginConfig(
    val pluginDescription: PluginDescription, private val eventHandlers: Map<EventType, Collection<VideoHandler>>,
    val searchShorthands: Map<String, String>, val allMetadata: List<MetadataRef<*>>
) {

    fun callHandlerFor(eventType: EventType, video: Video) {
        callHandlerFor(eventType, listOf(video))
    }

    fun callHandlerFor(eventType: EventType, videos: List<Video>) {
        val containerVideos = videos.map { VideoContainer.fromVideo(it) }
        eventHandlers.getOrDefault(eventType, Collections.emptyList()).sortedBy { it.priority.ordinal }.forEach { handler ->
            containerVideos.forEach { handler.call(it) }
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
        fun fromCreationContext(pluginCreateContext: PluginConfigurationEnvironment): PluginConfig {
            val description = de.eternalwings.vima.plugin.PluginDescription(
                pluginCreateContext.name, pluginCreateContext.description,
                pluginCreateContext.author, pluginCreateContext.version
            )
            return PluginConfig(
                description,
                pluginCreateContext.eventHandlers,
                pluginCreateContext.searchShorthands,
                pluginCreateContext.metadata
            )
        }
    }
}
