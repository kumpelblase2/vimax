package de.eternalwings.vima.plugin

import de.eternalwings.vima.plugin.api.MetadataRef
import de.eternalwings.vima.plugin.api.PluginConfigurationEnvironment
import java.util.Collections

data class PluginConfig(
    val pluginDescription: PluginDescription,
    val eventHandlers: Map<EventType, Collection<VideoHandler>>,
    val asyncEventHandlers: Map<EventType, Collection<AsyncVideoHandler>>,
    val searchShorthands: Map<String, String>,
    val allMetadata: List<MetadataRef<*>>
) {

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
                pluginCreateContext.eventHandlers.mapValues { (_, handlers) ->
                    handlers.sortedBy { it.priority.ordinal }
                },
                pluginCreateContext.asyncEventHandlers,
                pluginCreateContext.searchShorthands,
                pluginCreateContext.metadata
            )
        }
    }
}
