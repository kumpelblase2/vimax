package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.BooleanMetadataOptions
import de.eternalwings.vima.domain.DurationMetadataOptions
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.MetadataOptions
import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.NumberMetadataOptions
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.domain.SelectionValues
import de.eternalwings.vima.domain.TaglistMetadataOptions
import de.eternalwings.vima.domain.TextMetadataOptions
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.EventType.FINISH_WATCHING
import de.eternalwings.vima.plugin.EventType.START_WATCHING
import de.eternalwings.vima.plugin.EventType.UPDATE
import org.springframework.data.domain.Sort.Direction
import java.time.Duration
import java.util.Collections

fun registerPlugin(name: String, setup: PluginConfig.() -> Unit): PluginConfig {
    val config = PluginConfig(name)
    config.setup()
    return config
}

typealias VideoHandler = (Video) -> Unit

data class MetadataReference<T>(val name: String)

class PluginConfig internal constructor(val pluginName: String) {

    private val eventHandlers: MutableMap<EventType, MutableCollection<VideoHandler>> = mutableMapOf()
    var allMetadata: List<Metadata> = emptyList()
        private set

    fun <T> metadata(name: String, order: Direction, options: MetadataOptions<T>): MetadataReference<T> {
        val metadata = Metadata(name = name, type = options.type, systemSpecified = true, ordering = order, options = options)
        allMetadata = allMetadata + metadata
        return MetadataReference(name)
    }

    fun int(name: String, order: Direction, defaultValue: Int) = metadata(name, order, NumberMetadataOptions().also {
        it.defaultValue = defaultValue
    })

    fun text(name: String, order: Direction, defaultValue: String) = metadata(name, order, TextMetadataOptions().also {
        it.defaultValue = defaultValue
    })

    fun taglist(name: String, order: Direction, defaultValue: List<String> = emptyList()) =
            metadata(name, order, TaglistMetadataOptions().also { it.defaultValue = emptyList() })

    fun selection(name: String, order: Direction, values: List<SelectionValues>, defaultValue: SelectionValues) =
            metadata(name, order, SelectionMetadataOptions(values).also { it.defaultValue = defaultValue })

    fun duration(name: String, order: Direction, defaultValue: Duration) =
            metadata(name, order, DurationMetadataOptions().also { it.defaultValue = defaultValue })

    fun boolean(name: String, order: Direction, defaultValue: Boolean) =
            metadata(name, order, BooleanMetadataOptions().also { it.defaultValue = defaultValue })

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

    operator fun <T> Video.set(metadata: MetadataReference<T>, value: T) {
        check(hasMetadata(metadata.name))
        val existingMetadata = this.metadata ?: throw IllegalStateException()
        val existingContainer = existingMetadata.find { valueContainer ->
            valueContainer.definition?.name == metadata.name
        } ?: throw IllegalStateException()
        (existingContainer.value as MetadataValue<T>).value = value
    }

    operator fun <T> Video.get(metadata: MetadataReference<T>): T? {
        val existingMetadata = this.metadata ?: return null
        val existingContainer = existingMetadata.find { valueContainer ->
            valueContainer.definition?.name == metadata.name
        } ?: return null
        return (existingContainer.value as MetadataValue<T>).value ?: null
    }
}
