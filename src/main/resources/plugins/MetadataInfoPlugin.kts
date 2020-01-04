import de.eternalwings.vima.domain.SelectionValues
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.plugin.registerPlugin
import net.bramp.ffmpeg.FFprobe
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import java.time.Duration
import java.time.temporal.ChronoUnit

val undefinedResolution = SelectionValues("Unknown")
val SD = SelectionValues("SD")
val lowRes = SelectionValues("480p")
val higherRes = SelectionValues("720p")
val highestRes = SelectionValues("1080p")

registerPlugin("Metadata") {
    val allResolutions = listOf(undefinedResolution, SD, lowRes, higherRes, highestRes)
    val resolution = selection("Resolution", ASC, allResolutions, undefinedResolution)
    val bitRate = int("Bitrate", DESC, 0)
    val length = duration("Length", DESC, Duration.ZERO)

    val ffprobe = bindings["ffprobe"] as FFprobe

    val update: (Video) -> Unit = {
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
