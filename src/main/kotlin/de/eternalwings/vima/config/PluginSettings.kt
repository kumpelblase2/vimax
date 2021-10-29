package de.eternalwings.vima.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

@Configuration
class PluginSettings {

    @Value("\${external-plugin-dir:./plugins}")
    private lateinit var externalPathValue: Path

    val externalPath: Path
        get() = externalPathValue

}
