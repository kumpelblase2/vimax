package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.MetadataOptions
import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.EventType.FINISH_WATCHING
import de.eternalwings.vima.plugin.EventType.START_WATCHING
import de.eternalwings.vima.plugin.EventType.UPDATE
import org.springframework.data.domain.Sort.Direction
import java.util.Collections

fun registerPlugin(name: String, setup: PluginConfig.() -> Unit): PluginConfig {
    val config = PluginConfig(name)
    config.setup()
    return config
}

typealias VideoHandler = (Video) -> Unit

class PluginConfig internal constructor(val pluginName: String) {

    private val eventHandlers: MutableMap<EventType, MutableCollection<VideoHandler>> = mutableMapOf()
    var allMetadata: List<Metadata> = emptyList()
        private set

    fun <T> metadata(name: String, order: Direction, options: MetadataOptions<T>): Metadata {
        val metadata = Metadata(name = name, type = options.type, systemSpecified = true, ordering = order, options = options)
        allMetadata = allMetadata + metadata
        return metadata
    }

    private fun addHandler(eventType: EventType, handler: VideoHandler) {
        val handlers = eventHandlers.computeIfAbsent(eventType) { arrayListOf() }
        handlers.add(handler)
    }

    fun onCreate(handler: VideoHandler) {
        addHandler(CREATE, handler)
    }

    fun onUpdate(handler: VideoHandler) {
        addHandler(UPDATE, handler)
    }

    fun onStartWatching(handler: VideoHandler) {
        addHandler(START_WATCHING, handler)
    }

    fun onFinishWatching(handler: VideoHandler) {
        addHandler(FINISH_WATCHING, handler)
    }

    fun callHandlerFor(eventType: EventType, video: Video) {
        eventHandlers.getOrDefault(eventType, Collections.emptyList()).forEach { it(video) }
    }

    operator fun <T> Video.set(metadata: Metadata, value: T) {
        check(hasMetadata(metadata))
        val existingMetadata = this.metadata ?: throw IllegalStateException()
        val existingContainer = existingMetadata.find { valueContainer ->
            valueContainer.definition?.name == metadata.name
        } ?: throw IllegalStateException()
        (existingContainer.value as MetadataValue<T>).value = value
    }

    fun <T> Video.get(metadata: Metadata, defaultValue: T): T {
        val existingMetadata = this.metadata ?: return defaultValue
        val existingContainer = existingMetadata.find { valueContainer ->
            valueContainer.definition?.name == metadata.name
        } ?: return defaultValue
        return (existingContainer.value as MetadataValue<T>).value ?: defaultValue
    }
}
