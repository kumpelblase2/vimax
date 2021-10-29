package de.eternalwings.vima.plugin

import de.eternalwings.vima.config.PluginSettings
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

@Component
class PluginLoader(
    private val resourceLoader: ResourceLoader,
    pluginBindingProvider: PluginBindingsProvider,
    private val pluginSettings: PluginSettings
) {
    private val matcher = FileSystems.getDefault().getPathMatcher("glob:**.kts")
    private val executor = ScriptExecutor()

    private val externalPluginFiles: Stream<Path>
        get() {
            return if (!Files.exists(pluginSettings.externalPath) || !Files.isDirectory(pluginSettings.externalPath)) {
                Stream.empty()
            } else {
                Files.list(pluginSettings.externalPath).filter { matcher.matches(it) }
            }
        }

    init {
        PluginRegistration.setup(pluginBindingProvider)
    }

    fun getAvailablePlugins(): List<PluginSource> {
        val internalFileResources =
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:$internalPluginDir/*.kts")
        val internalPluginsStream = Stream.of(*internalFileResources).map { ResourcePluginSource(it) }
        val externalPluginsStream = externalPluginFiles.map { FileSystemPluginSource(it) }
        return Stream.concat(externalPluginsStream, internalPluginsStream).toList()
    }

    fun loadAllPlugins() {
        getAvailablePlugins().forEach { loadPluginAt(it) }
    }

    fun loadPluginAt(source: PluginSource) {
        PluginRegistration.prepareRegistration(source)
        source.openInputStream().use {
            executor.execute(it.reader())
        }
        PluginRegistration.cleanUpRegistration()
    }

    companion object {
        private const val internalPluginDir = "plugins"
    }
}
