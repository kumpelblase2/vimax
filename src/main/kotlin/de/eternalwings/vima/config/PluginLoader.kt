package de.eternalwings.vima.config

import de.eternalwings.vima.plugin.PluginManager
import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import java.io.Reader
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(value = ["disable-plugins"], havingValue = "false", matchIfMissing = true)
class PluginLoader(private val pluginManager: PluginManager, private val resourceLoader: ResourceLoader) {
    private val internalPluginDir = "plugins"

    @PostConstruct
    fun loadPlugins() {
        val resources =
                ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:$internalPluginDir/*.kts")
        val scriptLoader = KtsObjectLoader(this.javaClass.classLoader)
        resources.forEach { script ->
            script.inputStream.use { scriptLoader.execute(it.reader()) }
        }

        pluginManager.disableUnloaded()
    }

    fun KtsObjectLoader.execute(reader: Reader) {
        safeEval { engine.eval(reader) }
    }
}
