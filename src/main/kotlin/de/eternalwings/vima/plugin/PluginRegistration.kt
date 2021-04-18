package de.eternalwings.vima.plugin

import de.eternalwings.vima.plugin.api.PluginConfigurationEnvironment
import java.nio.file.Path

object PluginRegistration {
    private lateinit var pluginBindings: PluginBindings
    private val configs: MutableList<Pair<Path, PluginConfig>> = mutableListOf()

    private var nextPluginPath: Path? = null

    internal fun setup(bindings: PluginBindings) {
        this.pluginBindings = bindings
    }

    internal fun prepareRegistration(path: Path) {
        nextPluginPath = path
    }

    fun register(name: String, config: PluginConfigurationEnvironment.() -> Unit) {
        val path = nextPluginPath ?: throw IllegalStateException("Trying to register plugin outside of cycle")

        val context = PluginConfigurationEnvironment(name, pluginBindings)
        context.config()
        configs.add(path to PluginConfig.fromCreationContext(context))
    }

    internal fun cleanUpRegistration() {
        nextPluginPath = null
    }

    internal fun getAndClearRegistrationQueue(): List<Pair<Path, PluginConfig>> {
        val configCopy = ArrayList(configs)
        configs.clear()
        return configCopy
    }
}
