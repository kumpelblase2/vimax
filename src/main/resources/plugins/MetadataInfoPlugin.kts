import de.eternalwings.vima.domain.SelectionValue
import de.eternalwings.vima.plugin.PluginRegistration
import de.eternalwings.vima.plugin.VideoContainer
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import java.time.Duration
import java.time.temporal.ChronoUnit

val undefinedResolution = SelectionValue("Unknown")
val SD = SelectionValue("SD")
val lowRes = SelectionValue("480p")
val higherRes = SelectionValue("720p")
val highestRes = SelectionValue("1080p")

PluginRegistration.register("Metadata") {
    val allResolutions = listOf(undefinedResolution, SD, lowRes, higherRes, highestRes)
    val resolution = selection("Resolution", ASC, allResolutions, undefinedResolution)
    val bitRate = number("Bitrate", DESC, 0)
    val length = duration("Length", DESC, Duration.ZERO)

    val ffprobe = context.ffprobe

    val update: (VideoContainer) -> Unit = {
        val probe = ffprobe.probe(it.location)
        val videoStream = probe.streams[0]
        it[bitRate] = videoStream.bit_rate.toInt()
        it[length] = Duration.of(videoStream.duration.toLong(), ChronoUnit.SECONDS)
        it[resolution] = when {
            videoStream.height >= 1080 -> highestRes
            videoStream.height >= 720 -> higherRes
            videoStream.height >= 480 -> lowRes
            else -> SD
        }
    }

    onCreate(update)

    onUpdate {
        if (it[bitRate] == null || it[bitRate] == 0) {
            update(it)
        }
    }
}
