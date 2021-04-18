package de.eternalwings.vima.plugin

import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import java.io.Reader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.streams.toList

@Component
class PluginLoader(
    private val resourceLoader: ResourceLoader,
    pluginBindingProvider: PluginBindingProvider,
    @Value("\${external-plugin-dir}") private val externalPluginDir: Path
) {
    private val matcher = FileSystems.getDefault().getPathMatcher("glob:**.kts")
    private val scriptLoader = KtsObjectLoader(this.javaClass.classLoader)

    private val externalPluginFiles: Stream<Path>
        get() {
            return if (!Files.exists(externalPluginDir)) {
                Stream.empty()
            } else {
                Files.list(externalPluginDir).filter { matcher.matches(it) }
            }
        }

    init {
        PluginRegistration.setup(pluginBindingProvider.createBindings())
    }

    fun getAvailablePlugins(): List<Path> {
        val internalFileResources =
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:$internalPluginDir/*.kts")
        return Stream.concat(externalPluginFiles, Stream.of(*internalFileResources).map { it.file.toPath() }).toList()
    }

    fun loadAllPlugins() {
        getAvailablePlugins().forEach { loadPluginAt(it) }
    }

    fun loadPluginAt(path: Path) {
        PluginRegistration.prepareRegistration(path)
        Files.newInputStream(path).use {
            scriptLoader.execute(it.reader())
        }
        PluginRegistration.cleanUpRegistration()
    }

    private fun KtsObjectLoader.execute(reader: Reader) {
        kotlin.runCatching { engine.eval(reader) }.getOrElse { throw RuntimeException("Cannot load plugin", it) }
    }

    companion object {
        private const val internalPluginDir = "plugins"
    }
}
