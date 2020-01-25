import de.eternalwings.vima.plugin.PluginRegistration
import org.springframework.data.domain.Sort.Direction.ASC

PluginRegistration.register("Version") {
    description = "Show how often a video has been changed."
    author = "kumpelblase2"
    version = "1.0"

    val versionMetadata = number("Version", ASC, 1)

    onUpdate {
        it[versionMetadata] = (it[versionMetadata] ?: 0) + 1
    }
}
