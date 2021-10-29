package de.eternalwings.vima.process

import de.eternalwings.vima.config.ThumbnailSettings
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import java.util.stream.Collectors

@Component
class VideoThumbnailCreator(
    private val thumbnailGenerator: ThumbnailGenerator,
    private val thumbnailProcess: ThumbnailProcess,
    private val thumbnailSettings: ThumbnailSettings
) {

    private val rediscoverPattern: Pattern = Pattern.compile("^(.+)_\\d+\\.jpg$")

    fun createThumbnailsFor(videoPath: Path, videoId: Int) {
        val parentPath = videoPath.parent
        val thumbnailDir = parentPath.resolve(thumbnailSettings.relativePath)
        if (!Files.exists(thumbnailDir)) {
            Files.createDirectory(thumbnailDir)
        }
        val existingThumbnails =
            this.discoverExistingThumbnails(
                thumbnailDir, videoPath.fileName.toString()
                    .substringBeforeLast(".")
            )
        val remainingThumbnails = (thumbnailSettings.count - existingThumbnails.size).coerceAtLeast(0)
        val generatedThumbnails = thumbnailGenerator.generateThumbnailsFor(videoPath, thumbnailDir, remainingThumbnails)
        val allThumbnails = existingThumbnails + generatedThumbnails
        thumbnailProcess.addThumbnailsToVideo(videoId, allThumbnails.map { it.toString() })
    }

    private fun discoverExistingThumbnails(dir: Path, videoName: String): List<Path> {
        return Files.walk(dir, 1).filter { Files.isRegularFile(it) }.filter { file ->
            val matcher = rediscoverPattern.matcher(file.fileName.toString())
            matcher.matches() && matcher.group(1) == videoName
        }.collect(Collectors.toList())
    }
}
