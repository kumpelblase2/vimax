package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.PluginInformation
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.EventType.UPDATE
import de.eternalwings.vima.plugin.api.MetadataRef.OwnedMetadataRef
import de.eternalwings.vima.process.MetadataProcess
import de.eternalwings.vima.query.SearchShorthandProvider
import de.eternalwings.vima.repository.PluginInformationRepository
import de.eternalwings.vima.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class PluginManager(
    private val pluginRepository: PluginInformationRepository,
    private val videoRepository: VideoRepository,
    private val metadataProcess: MetadataProcess,
    private val shorthandProvider: SearchShorthandProvider,
    private val pluginsLoader: PluginLoader,
    private val pluginEventDispatcher: PluginEventDispatcher
) {
    private data class ActivePluginRegistration(
        val source: PluginSource,
        val information: PluginInformation,
        val config: PluginConfig
    )

    private var plugins: List<ActivePluginRegistration> = emptyList()

    private val enabledPlugins: List<ActivePluginRegistration>
        get() = plugins.filter { it.information.enabled }

    private fun addPlugin(source: PluginSource, information: PluginInformation, pluginConfig: PluginConfig) {
        if (plugins.any { it.information.name == information.name }) throw PluginAlreadyRegisteredException(information.name)
        val updatedInformation = pluginRepository.save(information)
        LOGGER.info("Loaded plugin ${updatedInformation.name}")
        plugins = plugins + ActivePluginRegistration(source, updatedInformation.copy(), pluginConfig)
    }

    private fun enablePluginFunctionality(pluginInformation: PluginInformation, pluginConfig: PluginConfig) {
        val existingMetadata = metadataProcess.getAll()
        pluginConfig.allMetadata.forEach { metadataRef ->
            val existing = existingMetadata.find { it.name == metadataRef.name }
            if (existing != null) {
                if (metadataRef is OwnedMetadataRef<*, *>) {
                    val owner = existing.owner
                    if (owner == null || owner.id != pluginInformation.id) {
                        throw IllegalStateException("Plugin ${pluginInformation.name} tried to register metadata ${metadataRef.name} but it already exists")
                    }
                    existing.ordering = metadataRef.ordering
                    existing.options = metadataRef.options
                    existing.readOnly = !metadataRef.editable
                    val updated = metadataProcess.createOrUpdate(existing)
                    metadataRef.assignId(updated.id!!)
                } else {
                    metadataRef.assignId(existing.id!!)
                }
            } else {
                if (metadataRef is OwnedMetadataRef<*, *>) {
                    val newMetadata = Metadata(
                        name = metadataRef.name,
                        type = metadataRef.options.type,
                        ordering = metadataRef.ordering,
                        readOnly = !metadataRef.editable,
                        owner = pluginInformation,
                        options = metadataRef.options
                    )
                    val saved = metadataProcess.createOrUpdate(newMetadata)
                    metadataRef.assignId(saved.id!!)
                } else {
                    throw IllegalStateException("Metadata ${metadataRef.name} does not exist but was referenced by ${pluginInformation.name}")
                }
            }
        }

        pluginConfig.searchShorthands.forEach { (name, replacement) ->
            shorthandProvider.registerShorthand(name, replacement, pluginInformation.id!!)
        }
    }

    private fun disablePluginFunctionality(pluginId: Int, pluginConfig: PluginConfig) {
        shorthandProvider.unregisterShorthandsOf(pluginId)
        pluginConfig.allMetadata.forEach { it.resetId() }
    }

    fun callEvent(eventType: EventType, video: Video): Boolean {
        return callEvent(eventType, listOf(video)).isNotEmpty()
    }

    fun callEvent(eventType: EventType, videos: List<Video>): Set<Int> {
        return if (enabledPlugins.isNotEmpty()) {
            val configs = enabledPlugins.map { it.config }
            val changed = pluginEventDispatcher.dispatchEvent(eventType, videos, configs)
            pluginEventDispatcher.dispatchEventAsync(eventType, videos, configs)
            changed
        } else {
            emptySet()
        }
    }

    fun disablePlugin(name: String) {
        val found = plugins.find { it.information.name == name } ?: return

        if (found.information.enabled) {
            found.information.disable()
            pluginRepository.updateEnabledState(
                name,
                found.information.enabled,
                found.information.enabledAt,
                found.information.disabledAt
            )
        }

        disablePluginFunctionality(found.information.id!!, found.config)
    }

    fun enablePlugin(name: String) {
        val found = plugins.find { it.information.name == name } ?: return
        enablePluginFunctionality(found.information, found.config)
        if (!found.information.enabled) {
            val disabledAt = found.information.disabledAt
            updateMissedVideos(found.config, disabledAt ?: LocalDateTime.now())
            found.information.enable()
            pluginRepository.updateEnabledState(
                name,
                found.information.enabled,
                found.information.enabledAt,
                found.information.disabledAt
            )
        }

    }

    private fun updateMissedVideos(pluginConfig: PluginConfig, afterTime: LocalDateTime) {
        val missedVideos = videoRepository.findVideosByUpdateTimeAfter(afterTime)
        if (pluginConfig.hasHandlerFor(UPDATE)) {
            pluginEventDispatcher.dispatchEvent(UPDATE, missedVideos, listOf(pluginConfig))
            videoRepository.saveAll(missedVideos)
        } else {
            val createdVideos = missedVideos.filter { it.creationTime!! > afterTime }
            pluginEventDispatcher.dispatchEvent(CREATE, createdVideos, listOf(pluginConfig))
            videoRepository.saveAll(createdVideos)
        }
    }

    @Transactional
    fun refreshPlugin(name: String) {
        LOGGER.info("Refreshing videos for plugin $name...")
        val plugin = plugins.find { it.information.name == name } ?: return
        val videos = videoRepository.findAll()
        val chunked = videos.chunked(100)
        chunked.forEachIndexed { index, chunk ->
            pluginEventDispatcher.dispatchEvent(UPDATE, chunk, listOf(plugin.config))
            LOGGER.info("Refresh: ${index * 100 + chunk.size}/${videos.size} done.")
        }
        videoRepository.saveAll(videos)
    }

    @Transactional
    fun reloadPlugin(name: String) {
        val toReload = plugins.find { it.information.name == name } ?: throw IllegalStateException("Plugin is not loaded.")
        plugins = plugins - toReload
        this.disablePluginFunctionality(toReload.information.id!!, toReload.config)

        this.pluginsLoader.loadPluginAt(toReload.source)
        val loadedPlugins = PluginRegistration.getAndClearRegistrationQueue()
        check(loadedPlugins.size == 1) {
            "Expected only one plugin to be reloaded."
        }

        val reloadedPlugin = loadedPlugins.first()
        val existingPluginInfo = pluginRepository.findByName(toReload.information.name!!)
        this.handlePluginLoaded(existingPluginInfo, reloadedPlugin.second, reloadedPlugin.first)
    }

    @Transactional
    fun loadAllPlugins() {
        pluginsLoader.loadAllPlugins()
        val loadedPlugins = PluginRegistration.getAndClearRegistrationQueue()
        val allExistingPlugins = pluginRepository.findAll()
        loadedPlugins.forEach { (path, config) ->
            val existingInfo = allExistingPlugins.find { it.name == config.pluginDescription.name }
            this.handlePluginLoaded(existingInfo, config, path)
        }

        val notRegisteredPlugins = allExistingPlugins.filter { all -> plugins.none { it.information.id == all.id } }
        notRegisteredPlugins.forEach { plugin ->
            LOGGER.info("Disabling plugin ${plugin.name} because it no longer exists.")
            pluginRepository.updateEnabledState(plugin.name!!, false, null, LocalDateTime.now())
        }
    }

    private fun handlePluginLoaded(existingInfo: PluginInformation?, config: PluginConfig, source: PluginSource) {
        val info = if (existingInfo == null) {
            config.pluginDescription.toInformation()
        } else {
            updateInfo(existingInfo, config)
            existingInfo
        }

        this.addPlugin(source, info, config)
        if (info.enabled) {
            LOGGER.info("Enabling plugin ${info.name}")
            this.enablePluginFunctionality(info, config)
        }
    }

    private fun updateInfo(existingInfo: PluginInformation, config: PluginConfig) {
        if (existingInfo.description.description != config.pluginDescription.description) {
            existingInfo.description.description = config.pluginDescription.description
        }

        if (existingInfo.description.author != config.pluginDescription.author) {
            existingInfo.description.author = config.pluginDescription.author
        }

        if (existingInfo.description.version != config.pluginDescription.version) {
            existingInfo.description.version = config.pluginDescription.version
        }
    }

    private fun PluginDescription.toInformation(): PluginInformation {
        return PluginInformation(this.name, information = de.eternalwings.vima.domain.PluginDescription().also {
            it.description = this.description
            it.author = this.author
            it.version = this.version
        })
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PluginManager::class.java)
    }
}

data class PluginAlreadyRegisteredException(val name: String?) :
    IllegalArgumentException("Plugin with name $name is already registered.")
