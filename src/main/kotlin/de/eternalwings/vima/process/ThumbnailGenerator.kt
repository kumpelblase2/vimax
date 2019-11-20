package de.eternalwings.vima.process

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.CustomFFmpegBuilder
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

@Component
class ThumbnailGenerator(private val ffmpeg: FFmpeg, private val ffprobe: FFprobe) {

    fun generateThumbnailsFor(video: Path, targetDirectory: Path, amount: Int): List<Path> {
        if(amount <= 0) return emptyList()

        val absoluteTargetPath = targetDirectory.toAbsolutePath()
        Files.createDirectories(absoluteTargetPath)
        val fileName = video.fileName.toString().substringBeforeLast(".")
        val length = getVideoLength(video)
        val timestamps = generateRandomTimestamps(length, amount)
        return timestamps.mapIndexed { index, timestamp ->
            val thumbnailFileName = fileName + "_" + index + ".jpg"
            val thumbnailImagePath = absoluteTargetPath.resolve(thumbnailFileName)
            val builder = CustomFFmpegBuilder()
                .addInput(video.toString())
                .setStartOffset(timestamp, MILLISECONDS)
                .addOutput(thumbnailImagePath.toString())
                .addExtraArgs("-frames:v", "1")
                .done()
            ffmpeg.run(builder)
            thumbnailImagePath
        }
    }

    private fun generateRandomTimestamps(length: Duration, amount: Int): List<Long> {
        val singleChunk = length.dividedBy(amount.toLong())
        var currentDuration = Duration.ZERO
        val timestamps = ArrayList<Long>()
        for (i in 1..amount) {
            val maximum = currentDuration.plus(singleChunk)
            val randomTimestamp = (currentDuration.toMillis() until maximum.toMillis()).random()
            timestamps.add(randomTimestamp)
            currentDuration = maximum
        }
        return timestamps
    }

    fun getVideoLength(video: Path): Duration {
        val probeResult = ffprobe.probe(video.toString())
        return Duration.of((probeResult.getFormat().duration * 1000).toLong(), ChronoUnit.MILLIS)
    }
}
