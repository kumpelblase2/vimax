package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.BooleanMetadataOptions
import de.eternalwings.vima.domain.DurationMetadataOptions
import de.eternalwings.vima.domain.FloatMetadataOptions
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.MetadataOptions
import de.eternalwings.vima.domain.NumberMetadataOptions
import de.eternalwings.vima.domain.PluginInformation
import de.eternalwings.vima.domain.RangeMetadataOptions
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.domain.SelectionValue
import de.eternalwings.vima.domain.TaglistMetadataOptions
import de.eternalwings.vima.domain.TextMetadataOptions
import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.EventType.FINISH_WATCHING
import de.eternalwings.vima.plugin.EventType.START_WATCHING
import de.eternalwings.vima.plugin.EventType.UPDATE
import de.eternalwings.vima.plugin.MetadataContainer.ExternalMetadata
import de.eternalwings.vima.plugin.MetadataContainer.OwnedMetadata
import de.eternalwings.vima.process.MetadataProcess
import org.springframework.data.domain.Sort.Direction
import java.time.Duration

class PluginCreateContext(private val metadataProcess: MetadataProcess, private val pluginInformation: PluginInformation,
                          val context: PluginExecutionContext) {
    internal val eventHandlers: MutableMap<EventType, MutableCollection<VideoHandler>> = mutableMapOf()
    internal val ownedMetadata: MutableList<MetadataContainer<*>> = arrayListOf()

    internal val name: String = pluginInformation.name!!

    private fun createMetadata(metadata: Metadata): MetadataInfo<*> {
        return MetadataInfo.fromMetadata(metadataProcess.createOrUpdate(metadata))
    }

    fun <T> metadata(name: String, order: Direction, options: MetadataOptions<T>): OwnedMetadata<T> {
        val existing = metadataProcess.getSimpleReference(name) ?: createMetadata(
                Metadata(name = name, type = options.type, ordering = order, readOnly = true, owner = pluginInformation,
                        options = options))
        if (existing.owner == null) {
            throw IllegalArgumentException("Such metadata already exists from the user.")
        } else if (existing.ownerId != pluginInformation.id) {
            throw IllegalArgumentException("Another plugin already provides this metadata: " + existing.owner)
        }

        val createdReference = OwnedMetadata(existing as MetadataInfo<T>)
        ownedMetadata.add(createdReference)
        return createdReference
    }

    fun number(name: String, order: Direction, defaultValue: Int) = metadata(name, order, NumberMetadataOptions().also {
        it.defaultValue = defaultValue
    })

    fun text(name: String, order: Direction, defaultValue: String) = metadata(name, order, TextMetadataOptions().also {
        it.defaultValue = defaultValue
    })

    fun taglist(name: String, order: Direction, defaultValue: List<String> = emptyList()) =
            metadata(name, order, TaglistMetadataOptions().also { it.defaultValue = defaultValue })

    fun selection(name: String, order: Direction, values: List<SelectionValue>, defaultValue: SelectionValue) =
            metadata(name, order, SelectionMetadataOptions(values).also { it.defaultValue = defaultValue })

    fun duration(name: String, order: Direction, defaultValue: Duration) =
            metadata(name, order, DurationMetadataOptions().also { it.defaultValue = defaultValue })

    fun switch(name: String, order: Direction, defaultValue: Boolean) =
            metadata(name, order, BooleanMetadataOptions().also { it.defaultValue = defaultValue })

    fun float(name: String, order: Direction, defaultValue: Double) =
            metadata(name, order, FloatMetadataOptions().also { it.defaultValue = defaultValue })

    fun range(name: String, order: Direction, defaultValue: Int, min: Int? = null, max: Int? = null, step: Int? = null) =
            metadata(name, order, RangeMetadataOptions(min, max, step).also { it.defaultValue = defaultValue })

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

    operator fun <T> VideoContainer.set(reference: OwnedMetadata<T>, value: T) {
        reference.set(this, value)
    }

    operator fun <T> VideoContainer.get(reference: MetadataContainer<T>): T? {
        return reference.get(this)
    }

    operator fun <T> VideoContainer.get(metadata: String): T? {
        val existing = metadataProcess.getSimpleReference(metadata) ?: return null
        return get(ExternalMetadata(existing as MetadataInfo<T>))
    }

}
