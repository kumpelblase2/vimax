import de.eternalwings.vima.plugin.registerPlugin
import org.springframework.data.domain.Sort.Direction.ASC

registerPlugin("Version") {
    val versionMetadata = int("Version", ASC, 1)

    onUpdate {
        it[versionMetadata] = (it[versionMetadata] ?: 0) + 1
    }
}
