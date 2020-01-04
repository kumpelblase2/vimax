package de.eternalwings.vima.config

import de.eternalwings.vima.plugin.PluginBindings
import de.eternalwings.vima.plugin.PluginConfig
import de.eternalwings.vima.plugin.PluginManager
import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PostConstruct
import javax.script.Bindings

@Component
class PluginLoader(private val pluginBindings: PluginBindings, private val pluginManager: PluginManager) {
    private val internalPluginDir = "plugins"

    @PostConstruct
    fun loadPlugins() {
        val classLoader = Thread.currentThread().contextClassLoader
        val classpathPluginDir = classLoader.getResource(internalPluginDir).path
        val bindings = pluginBindings.createBindings()
        val scriptLoader = KtsObjectLoader()
        Files.list(Paths.get(classpathPluginDir)).filter { Files.isRegularFile(it) }
            .filter { it.fileName.toString().endsWith("kts") }
            .map { script -> classLoader.getResourceAsStream(internalPluginDir + File.separator + script.fileName.toString()) }
            .filter { it != null }
            .forEach { script ->
                val config = script!!.use { scriptLoader.load(it.reader(), bindings, PluginConfig::class.java) }
                if(config == null) {
                    LOGGER.warn("Couldn't load plugin, it didn't return a plugin config.")
                } else {
                    pluginManager.registerPlugin(config)
                }
            }

        pluginManager.disableUnloaded()
    }

    fun <T> KtsObjectLoader.load(reader: Reader, bindings: Bindings, returnType: Class<T>): T? =
            safeEval { engine.eval(reader, bindings) } as T?

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PluginLoader::class.java)
    }
}
