package de.eternalwings.vima.plugin

import de.eternalwings.vima.plugin.api.PluginConfigurationEnvironment

object PluginRegistration {
    private lateinit var bindingsProvider: PluginBindingsProvider
    private val configs: MutableList<Pair<PluginSource, PluginConfig>> = mutableListOf()

    private var nextPluginSource: PluginSource? = null

    internal fun setup(bindings: PluginBindingsProvider) {
        this.bindingsProvider = bindings
    }

    internal fun prepareRegistration(pluginSource: PluginSource) {
        nextPluginSource = pluginSource
    }

    fun register(name: String, config: PluginConfigurationEnvironment.() -> Unit) {
        val path = nextPluginSource ?: throw IllegalStateException("Trying to register plugin outside of cycle")

        val context = PluginConfigurationEnvironment(name, bindingsProvider.createBindingsFor(name))
        context.config()
        configs.add(path to PluginConfig.fromCreationContext(context))
    }

    internal fun cleanUpRegistration() {
        nextPluginSource = null
    }

    internal fun getAndClearRegistrationQueue(): List<Pair<PluginSource, PluginConfig>> {
        val configCopy = ArrayList(configs)
        configs.clear()
        return configCopy
    }
}
