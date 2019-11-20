import de.eternalwings.vima.domain.NumberMetadataOptions
import de.eternalwings.vima.plugin.registerPlugin
import org.springframework.data.domain.Sort.Direction.ASC

registerPlugin("Version") {
    val versionMetadata = metadata("Version", ASC, NumberMetadataOptions().also { it.defaultValue = 1 })

    onUpdate {
        it[versionMetadata] = (it[versionMetadata] ?: 0) + 1
    }
}
