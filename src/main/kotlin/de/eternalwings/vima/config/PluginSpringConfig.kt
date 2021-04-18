package de.eternalwings.vima.config

import de.eternalwings.vima.plugin.PluginManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class PluginSpringConfig {

    @Value("\${disable-plugins:false}")
    private var pluginsDisabled: Boolean = false

    @Autowired
    private lateinit var pluginManager: PluginManager

    @PostConstruct
    fun handlePluginLoading() {
        if(!pluginsDisabled) {
            pluginManager.loadAllPlugins()
        }
    }
}
