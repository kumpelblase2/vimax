import de.eternalwings.vima.plugin.registerPlugin
import org.springframework.data.domain.Sort.Direction.DESC

registerPlugin("Watched") {
    val watchedMetadata = boolean("Watched", DESC, false)

    onStartWatching { video ->
        video[watchedMetadata] = true
    }
}
