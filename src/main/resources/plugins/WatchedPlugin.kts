import de.eternalwings.vima.plugin.PluginRegistration
import org.springframework.data.domain.Sort.Direction.DESC

PluginRegistration.register("Watched") {
    description = "Provide metadata about if a video was already watched."
    author = "kumpelblase2"
    version = "1.0"

    val watchedMetadata = switch("Watched", DESC, false)

    onStartWatching { video ->
        video[watchedMetadata] = true
    }
}
