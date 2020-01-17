import de.eternalwings.vima.plugin.PluginRegistration
import org.springframework.data.domain.Sort.Direction.ASC

PluginRegistration.register("Version") {
    val versionMetadata = number("Version", ASC, 1)

    onUpdate {
        it[versionMetadata] = (it[versionMetadata] ?: 0) + 1
    }
}
