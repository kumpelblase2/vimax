package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.Video
import org.slf4j.LoggerFactory

object PluginManager {
    private val LOGGER = LoggerFactory.getLogger(PluginManager::class.java)
    private var plugins: List<PluginConfig> = emptyList()

    fun registerPlugin(pluginConfig: PluginConfig) {
        if (getPlugin(pluginConfig.pluginName) != null) throw PluginAlreadyRegisteredException(pluginConfig.pluginName)

        plugins = plugins + pluginConfig
        LOGGER.debug("Registered plugin " + pluginConfig.pluginName)
    }

    fun getPlugin(name: String) = plugins.find { it.pluginName == name }

    fun callEvent(eventType: EventType, video: Video) {
        plugins.forEach { it.callHandlerFor(eventType, video) }
    }
}

data class PluginAlreadyRegisteredException(val name: String) :
        IllegalArgumentException("Plugin with name $name is already registered.")
