import de.eternalwings.vima.domain.NumberMetadataOptions
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.domain.SelectionValues
import de.eternalwings.vima.plugin.registerPlugin
import net.bramp.ffmpeg.FFprobe
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC

val undefinedResolution = SelectionValues("Unknown")
val SD = SelectionValues("SD")
val lowRes = SelectionValues("480p")
val higherRes = SelectionValues("720p")
val highestRes = SelectionValues("1080p")

registerPlugin("Metadata") {
    val allResolutions = listOf(undefinedResolution, SD, lowRes, higherRes, highestRes)
    val resolution = metadata("Resolution", ASC, SelectionMetadataOptions(allResolutions).also {
        it.defaultValue = undefinedResolution
    })
    val bitRate = metadata("Bitrate", DESC, NumberMetadataOptions().also { it.defaultValue = 0 })
    val length = metadata("Length", DESC, NumberMetadataOptions().also { it.defaultValue = 0 })

    val ffprobe = bindings["ffprobe"] as FFprobe

    onCreate {
        val probe = ffprobe.probe(it.location)
        val videoStream = probe.streams[0]
        it[bitRate] = videoStream.bit_rate.toInt()
        it[length] = videoStream.duration.toInt()
        it[resolution] = when {
            videoStream.height >= 1080 -> highestRes
            videoStream.height >= 720 -> higherRes
            videoStream.height >= 480 -> lowRes
            else -> SD
        }
    }
}
