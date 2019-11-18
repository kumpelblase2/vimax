import de.eternalwings.vima.domain.BooleanMetadataOptions
import de.eternalwings.vima.plugin.registerPlugin
import org.springframework.data.domain.Sort.Direction.DESC

registerPlugin("Watched") {
    val watchedMetadata = metadata("Watched", DESC, BooleanMetadataOptions().also { it.defaultValue = false })

    onStartWatching { video ->
        video[watchedMetadata] = true
    }
}
