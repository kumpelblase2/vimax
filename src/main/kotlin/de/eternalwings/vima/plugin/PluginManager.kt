package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.PluginInformation
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.EventType.UPDATE
import de.eternalwings.vima.process.MetadataProcess
import de.eternalwings.vima.query.SearchShorthandProvider
import de.eternalwings.vima.repository.PluginInformationRepository
import de.eternalwings.vima.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class PluginManager(private val pluginRepository: PluginInformationRepository,
                    private val videoRepository: VideoRepository,
                    private val metadataProcess: MetadataProcess,
                    private val shorthandProvider: SearchShorthandProvider,
                    pluginBindings: PluginBindings) {
    private var plugins: List<Pair<PluginInformation, PluginConfig>> = emptyList()

    init {
        PluginRegistration.setup(this, metadataProcess, pluginBindings)
    }

    private val enabledPlugins: List<Pair<PluginInformation, PluginConfig>>
        get() = plugins.filter { it.first.enabled }

    fun getOrCreatePlugin(name: String): PluginInformation {
        return pluginRepository.findByName(name) ?: createPlugin(name)
    }

    fun addPlugin(information: PluginInformation, pluginConfig: PluginConfig) {
        val updatedInformation = pluginRepository.save(information)
        if (plugins.any { it.first.name == updatedInformation.name }) throw PluginAlreadyRegisteredException(updatedInformation.name)
        LOGGER.info("Loaded plugin ${updatedInformation.name}")
        pluginConfig.searchShorthands.forEach { (name, replacement) -> shorthandProvider.registerShorthand(name, replacement) }
        plugins = plugins + (updatedInformation to pluginConfig)
    }

    private fun createPlugin(name: String): PluginInformation {
        return pluginRepository.save(PluginInformation(name))
    }

    fun callEvent(eventType: EventType, video: Video) {
        callEvent(eventType, listOf(video))
    }

    fun callEvent(eventType: EventType, videos: List<Video>) {
        enabledPlugins.forEach { it.second.callHandlerFor(eventType, videos) }
    }

    fun disablePlugin(name: String) {
        val found = plugins.find { it.first.name == name } ?: return
        if (found.first.enabled) {
            found.first.disable()
            pluginRepository.updateEnabledState(name, found.first.enabled, found.first.enabledAt, found.first.disabledAt)
        }
    }

    fun enablePlugin(name: String) {
        val found = plugins.find { it.first.name == name } ?: return
        if (!found.first.enabled) {
            val disabledAt = found.first.disabledAt
            updateMissedVideos(found.second, disabledAt ?: LocalDateTime.now())
            found.first.enable()
            pluginRepository.updateEnabledState(name, found.first.enabled, found.first.enabledAt, found.first.disabledAt)
        }
    }

    private fun updateMissedVideos(pluginConfig: PluginConfig, afterTime: LocalDateTime) {
        val missedVideos = videoRepository.findVideosByUpdateTimeAfter(afterTime)
        if (pluginConfig.hasHandlerFor(UPDATE)) {
            missedVideos.forEach { pluginConfig.callHandlerFor(UPDATE, it) }
            videoRepository.saveAll(missedVideos)
        } else {
            val createdVideos = missedVideos.filter { it.creationTime!! > afterTime }
            createdVideos.forEach { pluginConfig.callHandlerFor(EventType.CREATE, it) }
            videoRepository.saveAll(createdVideos)
        }
    }

    fun refreshPlugin(name: String) {
        LOGGER.info("Refreshing videos for plugin $name...")
        val plugin = plugins.find { it.first.name == name } ?: return
        val videos = videoRepository.findAll()
        val chunked = videos.chunked(100)
        chunked.forEachIndexed { index, chunk ->
            plugin.second.callHandlerFor(UPDATE, chunk)
            LOGGER.info("Refresh: ${index * 100 + chunk.size}/${videos.size} done.")
        }
        videoRepository.saveAll(videos)
    }

    @Transactional
    fun disableUnloaded() {
        val allPlugins = pluginRepository.findAll()
        val notRegisteredPlugins = allPlugins.filter { all -> plugins.none { it.first.id == all.id } }
        notRegisteredPlugins.forEach { plugin ->
            pluginRepository.updateEnabledState(plugin.name!!, false, null, LocalDateTime.now())
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PluginManager::class.java)
    }
}

data class PluginAlreadyRegisteredException(val name: String?) :
        IllegalArgumentException("Plugin with name $name is already registered.")
