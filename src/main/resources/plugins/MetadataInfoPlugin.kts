import de.eternalwings.vima.domain.SelectionValue
import de.eternalwings.vima.plugin.PluginPriority.NORMAL
import de.eternalwings.vima.plugin.PluginRegistration
import de.eternalwings.vima.plugin.VideoContainer
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import java.time.Duration
import java.time.temporal.ChronoUnit

val undefinedResolution = SelectionValue(1, "Unknown")
val SD = SelectionValue(2, "SD")
val lowRes = SelectionValue(3, "480p")
val higherRes = SelectionValue(4, "720p")
val highestRes = SelectionValue(5, "1080p")

PluginRegistration.register("Metadata") {
    description = "Show video file metadata provided by ffmpeg."
    author = "kumpelblase2"
    version = "1.0"

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
            videoStream.height >= 1080 -> highestRes.id
            videoStream.height >= 720 -> higherRes.id
            videoStream.height >= 480 -> lowRes.id
            else -> SD.id
        }!!
    }

    onCreate(NORMAL, update)

    onUpdate {
        if (it[bitRate] == null || it[bitRate] == 0) {
            update(it)
        }
    }
}
