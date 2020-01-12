package de.eternalwings.vima.config

import de.eternalwings.vima.plugin.PluginManager
import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import java.io.Reader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(value = ["disable-plugins"], havingValue = "false", matchIfMissing = true)
class PluginLoader(private val pluginManager: PluginManager, private val resourceLoader: ResourceLoader,
                   @Value("\${external-plugin-dir}") private val externalPluginDir: Path) {
    private val matcher = FileSystems.getDefault().getPathMatcher("glob:**.kts")
    private val internalPluginDir = "plugins"

    private val externalPluginFiles: Stream<Path>
        get() {
            return if (!Files.exists(externalPluginDir)) {
                Stream.empty()
            } else {
                Files.list(externalPluginDir).filter { matcher.matches(it) }
            }
        }

    @PostConstruct
    fun loadPlugins() {
        val resources =
                ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:$internalPluginDir/*.kts")
        val scriptLoader = KtsObjectLoader(this.javaClass.classLoader)
        val resourceInputStreams = Stream.of(*resources).map { it.inputStream }
        val externalInputStreams = externalPluginFiles.map { Files.newInputStream(it) }

        Stream.concat(resourceInputStreams, externalInputStreams).forEach { script ->
            script.use { scriptLoader.execute(it.reader()) }
        }

        pluginManager.disableUnloaded()
    }

    fun KtsObjectLoader.execute(reader: Reader) {
        safeEval { engine.eval(reader) }
    }
}
