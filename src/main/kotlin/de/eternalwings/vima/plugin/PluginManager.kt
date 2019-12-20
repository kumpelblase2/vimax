package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.PluginInformation
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.process.VideoMetadataUpdater
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.PluginInformationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PluginManager(private val pluginRepository: PluginInformationRepository, private val metadataRepository: MetadataRepository,
                    private val videoMetadataUpdater: VideoMetadataUpdater) {
    private var plugins: List<Pair<PluginInformation, PluginConfig>> = emptyList()

    private val enabledPlugins: List<Pair<PluginInformation, PluginConfig>>
        get() = plugins.filter { it.first.enabled }

    fun registerPlugin(pluginConfig: PluginConfig) {
        if (getPlugin(pluginConfig.pluginName) != null) throw PluginAlreadyRegisteredException(pluginConfig.pluginName)
        val plugin = pluginRepository.findByName(pluginConfig.pluginName) ?: createPlugin(pluginConfig)

        plugins = plugins + (plugin.copy() to pluginConfig)
        LOGGER.debug("Registered plugin " + pluginConfig.pluginName)
    }

    private fun createPlugin(pluginConfig: PluginConfig): PluginInformation {
        val createdPlugin = pluginRepository.save(PluginInformation(pluginConfig.pluginName))
        registerMetadata(pluginConfig.allMetadata)
        return createdPlugin
    }

    fun getPlugin(name: String) = plugins.find { it.second.pluginName == name }

    fun callEvent(eventType: EventType, video: Video) {
        enabledPlugins.forEach { it.second.callHandlerFor(eventType, video) }
    }

    fun disablePlugin(name: String) {
        val found = plugins.find { it.first.name == name } ?: return
        found.first.disable()
        pluginRepository.updateEnabledState(name, found.first.enabled, found.first.enabledAt, found.first.disabledAt)
    }

    fun enablePlugin(name: String) {
        val found = plugins.find { it.first.name == name } ?: return
        val disabledAt = found.first.disabledAt
        updateMissedVideos(found.second, disabledAt ?: LocalDateTime.now())
        found.first.enable()
        pluginRepository.updateEnabledState(name, found.first.enabled, found.first.enabledAt, found.first.disabledAt)
    }

    private fun updateMissedVideos(pluginConfig: PluginConfig, afterTime: LocalDateTime) {
        // TODO
    }

    private fun registerMetadata(metadata: Collection<Metadata>) {
        var highestDisplayOrder = metadataRepository.getHighestDisplayOrder() ?: 0
        val existingMetadata = metadataRepository.findAll()
        val newMetadata = metadata.filter { new -> existingMetadata.none { it.name == new.name } }
        for (newMetadatum in newMetadata) {
            highestDisplayOrder += 1
            newMetadatum.displayOrder = highestDisplayOrder
        }
        val saved = metadataRepository.saveAll(newMetadata)
        saved.forEach { videoMetadataUpdater.addMetadata(it) }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PluginManager::class.java)
    }
}

data class PluginAlreadyRegisteredException(val name: String) :
        IllegalArgumentException("Plugin with name $name is already registered.")
