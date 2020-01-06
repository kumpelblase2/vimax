package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.process.MetadataProcess

class MetadataInfo<T>(
        val id: Int,
        val name: String,
        val defaultValue: T?,
        val owner: String?,
        val ownerId: Int?
) {
    companion object {
        fun fromMetadata(metadata: Metadata): MetadataInfo<*> {
            return MetadataInfo(metadata.id!!, metadata.name!!, metadata.options!!.defaultValue, metadata.owner?.name,
                    metadata.owner?.id)
        }
    }
}

sealed class MetadataContainer<T>(protected val metadata: MetadataInfo<T>) {
    fun get(video: VideoContainer): T? {
        val defaultValue = this.metadata.defaultValue
        val existingMetadata = video.metadata
        val value = existingMetadata[this.metadata.id] as? MetadataValue<T> ?: return defaultValue
        return value.value
    }

    class ExternalMetadata<T>(metadata: MetadataInfo<T>) : MetadataContainer<T>(metadata)
    class OwnedMetadata<T>(metadata: MetadataInfo<T>) : MetadataContainer<T>(metadata) {
        fun set(video: VideoContainer, value: T) {
            check(video.hasMetadata(this.metadata.id))
            val existingMetadata = video.metadata
            val existingContainer = existingMetadata[this.metadata.id] ?: throw IllegalStateException()
            (existingContainer as MetadataValue<T>).value = value
        }
    }
}

typealias VideoHandler = (VideoContainer) -> Unit

object PluginRegistration {
    private lateinit var pluginManager: PluginManager
    private lateinit var metadataProcess: MetadataProcess
    private lateinit var pluginExecutionContext: PluginExecutionContext

    internal fun setup(pluginManager: PluginManager, metadataProcess: MetadataProcess, pluginBindings: PluginBindings) {
        this.pluginManager = pluginManager
        this.metadataProcess = metadataProcess
        this.pluginExecutionContext = pluginBindings.createBindings()
    }

    fun register(name: String, config: PluginCreateContext.() -> Unit) {
        val info = pluginManager.getOrCreatePlugin(name)
        val context =
                PluginCreateContext(metadataProcess, info, pluginExecutionContext)
        context.config()
        pluginManager.addPlugin(info, PluginConfig.fromCreationContext(context))
    }
}
