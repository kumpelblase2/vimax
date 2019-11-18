package de.eternalwings.vima.config

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.plugin.PluginConfig
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.process.VideoMetadataUpdater
import de.eternalwings.vima.repository.MetadataRepository
import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PostConstruct

@Component
class PluginLoader(private val metadataRepository: MetadataRepository, private val videoMetadataUpdater: VideoMetadataUpdater) {
    private val internalPluginDir = "plugins"

    @PostConstruct
    fun loadPlugins() {
        val classLoader = Thread.currentThread().contextClassLoader
        val classpathPluginDir = classLoader.getResource(internalPluginDir).path
        Files.list(Paths.get(classpathPluginDir)).filter { Files.isRegularFile(it) }
            .filter { it.fileName.toString().endsWith("kts") }
            .map { script -> classLoader.getResourceAsStream(internalPluginDir + File.separator + script.fileName.toString()) }
            .filter { it != null }
            .forEach { script ->
                val config = script!!.use { KtsObjectLoader().load<PluginConfig>(it) }
                PluginManager.registerPlugin(config)
                registerMetadata(config.allMetadata)
            }
    }

    private fun registerMetadata(metadata: Collection<Metadata>) {
        val existingMetadata = metadataRepository.findAll()
        val newMetadata = metadata.filter { new -> existingMetadata.none { it.name == new.name } }
        val saved = metadataRepository.saveAll(newMetadata)
        saved.forEach { videoMetadataUpdater.addMetadata(it) }
    }
}
