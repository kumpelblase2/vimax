package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.PluginInformation
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.process.VideoMetadataUpdater
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.PluginInformationRepository
import de.eternalwings.vima.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class PluginManager(private val pluginRepository: PluginInformationRepository, private val metadataRepository: MetadataRepository,
                    private val videoMetadataUpdater: VideoMetadataUpdater, private val videoRepository: VideoRepository) {
    private var plugins: List<Pair<PluginInformation, PluginConfig>> = emptyList()

    private val enabledPlugins: List<Pair<PluginInformation, PluginConfig>>
        get() = plugins.filter { it.first.enabled }

    fun registerPlugin(pluginConfig: PluginConfig) {
        if (getPlugin(pluginConfig.pluginName) != null) throw PluginAlreadyRegisteredException(pluginConfig.pluginName)
        val plugin = pluginRepository.findByName(pluginConfig.pluginName) ?: createPlugin(pluginConfig)
        registerMetadata(plugin, pluginConfig.allMetadata)

        plugins = plugins + (plugin.copy() to pluginConfig)
        LOGGER.debug("Registered plugin " + pluginConfig.pluginName)
    }

    private fun createPlugin(pluginConfig: PluginConfig): PluginInformation {
        return pluginRepository.save(PluginInformation(pluginConfig.pluginName))
    }

    fun getPlugin(name: String) = plugins.find { it.second.pluginName == name }

    fun callEvent(eventType: EventType, video: Video) {
        enabledPlugins.forEach { it.second.callHandlerFor(eventType, video) }
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
        if (pluginConfig.hasHandlerFor(EventType.UPDATE)) {
            missedVideos.forEach { pluginConfig.callHandlerFor(EventType.UPDATE, it) }
            videoRepository.saveAll(missedVideos)
        } else {
            val createdVideos = missedVideos.filter { it.creationTime!! > afterTime }
            createdVideos.forEach { pluginConfig.callHandlerFor(EventType.CREATE, it) }
            videoRepository.saveAll(createdVideos)
        }
    }

    private fun registerMetadata(owner: PluginInformation, metadata: Collection<Metadata>) {
        val existingMetadata = metadataRepository.findAll()
        val newMetadata = metadata.filter { new -> existingMetadata.none { it.name == new.name } }
        val oldMetadata = existingMetadata.filter { old -> metadata.any { it.name == old.name } }
        val withoutOwner = oldMetadata.filter { it.owner == null }
        if (withoutOwner.isNotEmpty()) {
            withoutOwner.forEach { it.owner = owner }
            metadataRepository.saveAll(withoutOwner)
        }

        if (newMetadata.isNotEmpty()) {
            var highestDisplayOrder = metadataRepository.getHighestDisplayOrder() ?: 0
            for (newMetadatum in newMetadata) {
                highestDisplayOrder += 1
                newMetadatum.displayOrder = highestDisplayOrder
                newMetadatum.owner = owner
            }
            val saved = metadataRepository.saveAll(newMetadata)
            saved.forEach { videoMetadataUpdater.addMetadata(it) }
        }
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

data class PluginAlreadyRegisteredException(val name: String) :
        IllegalArgumentException("Plugin with name $name is already registered.")
