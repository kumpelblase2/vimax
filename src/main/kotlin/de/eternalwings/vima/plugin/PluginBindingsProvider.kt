package de.eternalwings.vima.plugin

interface PluginBindingsProvider {
    fun createBindingsFor(name: String): PluginBindings
}
