import de.eternalwings.vima.plugin.PluginRegistration
import org.springframework.data.domain.Sort.Direction.DESC

PluginRegistration.register("Watched") {
    val watchedMetadata = switch("Watched", DESC, false)

    onStartWatching { video ->
        video[watchedMetadata] = true
    }
}
