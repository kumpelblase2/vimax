package de.eternalwings.vima.config

import de.eternalwings.vima.plugin.PluginManager
import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.File
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(value = ["disable-plugins"], havingValue = "false", matchIfMissing = true)
class PluginLoader(private val pluginManager: PluginManager) {
    private val internalPluginDir = "plugins"

    @PostConstruct
    fun loadPlugins() {
        val classLoader = Thread.currentThread().contextClassLoader
        val classpathPluginDir = classLoader.getResource(internalPluginDir).path
        val scriptLoader = KtsObjectLoader()
        Files.list(Paths.get(classpathPluginDir)).filter { Files.isRegularFile(it) }
            .filter { it.fileName.toString().endsWith("kts") }
            .map { script -> classLoader.getResourceAsStream(internalPluginDir + File.separator + script.fileName.toString()) }
            .filter { it != null }
            .forEach { script ->
                script!!.use { scriptLoader.execute(it.reader()) }
            }

        pluginManager.disableUnloaded()
    }

    fun KtsObjectLoader.execute(reader: Reader) {
        safeEval { engine.eval(reader) }
    }
}
