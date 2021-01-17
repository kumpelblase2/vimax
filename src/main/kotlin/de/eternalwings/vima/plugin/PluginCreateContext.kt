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
import de.eternalwings.vima.ext.prop
import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.EventType.FINISH_WATCHING
import de.eternalwings.vima.plugin.EventType.START_WATCHING
import de.eternalwings.vima.plugin.EventType.UPDATE
import de.eternalwings.vima.plugin.MetadataContainer.ExternalMetadata
import de.eternalwings.vima.plugin.MetadataContainer.OwnedMetadata
import de.eternalwings.vima.plugin.PluginPriority.NORMAL
import de.eternalwings.vima.process.MetadataProcess
import org.springframework.data.domain.Sort.Direction
import java.time.Duration

class PluginCreateContext(private val metadataProcess: MetadataProcess, private val pluginInformation: PluginInformation,
                          val context: PluginExecutionContext) {
    internal val eventHandlers: MutableMap<EventType, MutableCollection<VideoHandler>> = mutableMapOf()
    internal val ownedMetadata: MutableList<MetadataContainer<*>> = arrayListOf()

    internal val name: String = pluginInformation.name!!
    var description by prop(pluginInformation.description::description)
    var author by prop(pluginInformation.description::author)
    var version by prop(pluginInformation.description::version)

    private fun createMetadata(metadata: Metadata): MetadataInfo<*> {
        return MetadataInfo.fromMetadata(metadataProcess.createOrUpdate(metadata))
    }

    fun <T,S> metadata(name: String, order: Direction, options: MetadataOptions<T,S>): OwnedMetadata<T, S> {
        val existing = metadataProcess.getSimpleReference(name) ?: createMetadata(
                Metadata(name = name, type = options.type, ordering = order, readOnly = true, owner = pluginInformation,
                        options = options))
        if (existing.owner == null) {
            throw IllegalArgumentException("Such metadata already exists from the user.")
        } else if (existing.ownerId != pluginInformation.id) {
            throw IllegalArgumentException("Another plugin already provides this metadata: " + existing.owner)
        }

        val createdReference = OwnedMetadata<T,S>(existing as MetadataInfo<T>)
        ownedMetadata.add(createdReference)
        return createdReference
    }

    fun number(name: String, order: Direction, defaultValue: Int) =
            metadata(name, order, NumberMetadataOptions().also { it.defaultValue = defaultValue })

    fun text(name: String, order: Direction, defaultValue: String) =
            metadata(name, order, TextMetadataOptions().also { it.defaultValue = defaultValue })

    fun taglist(name: String, order: Direction, defaultValue: Set<String> = emptySet()) =
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

    private fun addHandler(eventType: EventType, priority: PluginPriority, handler: VideoHandlerCall) {
        val handlers = eventHandlers.computeIfAbsent(eventType) { arrayListOf() }
        handlers.add(VideoHandler(priority, handler))
    }

    fun onCreate(priority: PluginPriority = NORMAL, handler: VideoHandlerCall) {
        addHandler(CREATE, priority, handler)
    }

    fun onUpdate(priority: PluginPriority = NORMAL, handler: VideoHandlerCall) {
        addHandler(UPDATE, priority, handler)
    }

    fun onStartWatching(priority: PluginPriority = NORMAL, handler: VideoHandlerCall) {
        addHandler(START_WATCHING, priority, handler)
    }

    fun onFinishWatching(priority: PluginPriority = NORMAL, handler: VideoHandlerCall) {
        addHandler(FINISH_WATCHING, priority, handler)
    }

    operator fun <T,S> VideoContainer.set(reference: OwnedMetadata<T,S>, value: S) {
        reference.set(this, value)
    }

    fun <T> VideoContainer.set(name: String, updater: (T?) -> T?) {
        val existing = metadataProcess.getSimpleReference(name) ?: return
        ExternalMetadata(existing as MetadataInfo<T>).update(this, updater)
    }

    operator fun <T> VideoContainer.get(reference: MetadataContainer<T>): T? {
        return reference.get(this)
    }

    operator fun <T> VideoContainer.get(metadata: String): T? {
        val existing = metadataProcess.getSimpleReference(metadata) ?: return null
        return get(ExternalMetadata(existing as MetadataInfo<T>))
    }

}
