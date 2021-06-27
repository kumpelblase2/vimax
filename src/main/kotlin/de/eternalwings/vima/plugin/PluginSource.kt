package de.eternalwings.vima.plugin

import org.springframework.core.io.Resource
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

interface PluginSource {
    fun openInputStream(): InputStream
}

data class ResourcePluginSource(private val resource: Resource) : PluginSource {
    override fun openInputStream(): InputStream {
        return resource.inputStream
    }
}

data class FileSystemPluginSource(private val path: Path) : PluginSource {
    override fun openInputStream(): InputStream {
        return Files.newInputStream(path)
    }
}
