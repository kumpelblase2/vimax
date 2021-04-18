package de.eternalwings.vima.plugin.api

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parsed
import de.eternalwings.vima.domain.BooleanMetadataOptions
import de.eternalwings.vima.domain.DurationMetadataOptions
import de.eternalwings.vima.domain.FloatMetadataOptions
import de.eternalwings.vima.domain.MetadataOptions
import de.eternalwings.vima.domain.NumberMetadataOptions
import de.eternalwings.vima.domain.RangeMetadataOptions
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.domain.SelectionValue
import de.eternalwings.vima.domain.TaglistMetadataOptions
import de.eternalwings.vima.domain.TextMetadataOptions
import de.eternalwings.vima.plugin.EventType
import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.EventType.FINISH_WATCHING
import de.eternalwings.vima.plugin.EventType.START_WATCHING
import de.eternalwings.vima.plugin.EventType.UPDATE
import de.eternalwings.vima.plugin.PluginBindings
import de.eternalwings.vima.plugin.PluginPriority
import de.eternalwings.vima.plugin.PluginPriority.NORMAL
import de.eternalwings.vima.plugin.VideoContainer
import de.eternalwings.vima.plugin.VideoHandler
import de.eternalwings.vima.plugin.VideoHandlerCall
import de.eternalwings.vima.plugin.api.MetadataRef.OwnedMetadataRef
import de.eternalwings.vima.plugin.api.MetadataRef.SharedMetadataRef
import de.eternalwings.vima.query.QueryParser
import org.springframework.data.domain.Sort.Direction
import java.time.Duration

@Suppress("unused")
class PluginConfigurationEnvironment(internal val name: String, val context: PluginBindings) {
    internal val eventHandlers: MutableMap<EventType, MutableCollection<VideoHandler>> = mutableMapOf()
    internal val searchShorthands: MutableMap<String, String> = mutableMapOf()
    internal val metadata: MutableList<MetadataRef<*>> = mutableListOf()
    private var finished = false

    var description: String? = null
    var author: String? = null
    var version: String? = null

    private fun checkConfigurable() {
        check(!finished) {
            "Tried to register metadata after configuration ran."
        }
    }

    private fun <T, S> createMetadata(name: String, order: Direction, options: MetadataOptions<T, S>): MetadataRef<S> {
        checkConfigurable()
        val ref = OwnedMetadataRef(name, order, options)
        metadata.add(ref)
        return ref
    }

    private fun <T> createSharedMetadata(name: String): MetadataRef<T> {
        checkConfigurable()
        val ref = SharedMetadataRef<T>(name)
        metadata.add(ref)
        return ref
    }

    fun number(name: String, order: Direction, defaultValue: Int): MetadataRef<Int> {
        return createMetadata(name, order, NumberMetadataOptions().also { it.defaultValue = defaultValue })
    }

    fun numberRef(name: String): MetadataRef<Int> {
        return createSharedMetadata(name)
    }

    fun text(name: String, order: Direction, defaultValue: String): MetadataRef<String> {
        return createMetadata(name, order, TextMetadataOptions().also { it.defaultValue = defaultValue })
    }

    fun textRef(name: String): MetadataRef<Int> {
        return createSharedMetadata(name)
    }

    fun taglist(name: String, order: Direction, defaultValue: Set<String> = emptySet()): MetadataRef<Set<String>> {
        return createMetadata(name, order, TaglistMetadataOptions().also { it.defaultValue = defaultValue })
    }

    fun taglistRef(name: String): MetadataRef<Set<String>> {
        return createSharedMetadata(name)
    }

    fun selection(
        name: String,
        order: Direction,
        values: List<SelectionValue>,
        defaultValue: SelectionValue
    ): MetadataRef<Int> {
        return createMetadata(name, order, SelectionMetadataOptions(values).also { it.defaultValue = defaultValue })
    }

    fun selectionRef(name: String): MetadataRef<Int> {
        return createSharedMetadata(name)
    }

    fun duration(name: String, order: Direction, defaultValue: Duration): MetadataRef<Duration> {
        return createMetadata(name, order, DurationMetadataOptions().also { it.defaultValue = defaultValue })
    }

    fun durationRef(name: String): MetadataRef<Duration> {
        return createSharedMetadata(name)
    }

    fun switch(name: String, order: Direction, defaultValue: Boolean): MetadataRef<Boolean> {
        return createMetadata(name, order, BooleanMetadataOptions().also { it.defaultValue = defaultValue })
    }

    fun switchRef(name: String): MetadataRef<Boolean> {
        return createSharedMetadata(name)
    }

    fun float(name: String, order: Direction, defaultValue: Double): MetadataRef<Double> {
        return createMetadata(name, order, FloatMetadataOptions().also { it.defaultValue = defaultValue })
    }

    fun floatRef(name: String): MetadataRef<Double> {
        return createSharedMetadata(name)
    }

    fun range(
        name: String,
        order: Direction,
        defaultValue: Int,
        min: Int? = null,
        max: Int? = null,
        step: Int? = null
    ): MetadataRef<Int> {
        return createMetadata(name, order, RangeMetadataOptions(min, max, step).also { it.defaultValue = defaultValue })
    }

    private fun addHandler(eventType: EventType, priority: PluginPriority, handler: VideoHandlerCall) {
        checkConfigurable()
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

    fun registerSearchShorthand(name: String, result: String) {
        checkConfigurable()
        if (QueryParser.tryParseToEnd(result) !is Parsed) {
            throw IllegalArgumentException("Not a valid query for replacement")
        }

        searchShorthands[name] = result
    }

    operator fun <T> VideoContainer.set(metadata: MetadataRef<T>, value: T?) {
        metadata.assignValueOn(this, value)
    }

    operator fun <T> VideoContainer.get(metadata: MetadataRef<T>): T? {
        return metadata.getValueOf(this)
    }
}
