package de.eternalwings.vima.process

import de.eternalwings.vima.repository.ThumbnailRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import java.util.stream.Collectors

@Component
class VideoThumbnailCreator(private val thumbnailGenerator: ThumbnailGenerator,
                            private val thumbnailRepository: ThumbnailRepository,
                            @Value("\${thumbnail-amount:3}") private val thumbnailCount: Int,
                            @Value("\${thumbnail-relative-dir:.thumbnails}") thumbnailsRelativePath: String) {

    private val rediscoverPattern: Pattern = Pattern.compile("(.+)_\\d+\\.jpg")
    private val relativeThumbnailDir: Path = Paths.get(thumbnailsRelativePath)

    fun createThumbnailsFor(videoPath: Path, videoId: Int) {
        val parentPath = videoPath.parent
        val thumbnailDir = parentPath.resolve(relativeThumbnailDir)
        if(!Files.exists(thumbnailDir)) {
            Files.createDirectory(thumbnailDir)
        }
        val existingThumbnails =
                this.discoverExistingThumbnails(thumbnailDir, videoPath.fileName.toString()
                    .substringBeforeLast("."))
        val remainingThumbnails = Math.max(thumbnailCount - existingThumbnails.size, 0)
        val allThumbnails = existingThumbnails + thumbnailGenerator.generateThumbnailsFor(videoPath, thumbnailDir,
                remainingThumbnails)
        allThumbnails.forEach { thumb ->
            thumbnailRepository.insertThumbnailForVideo(thumb.toString(), videoId)
        }
        thumbnailRepository.flush()
    }

    private fun discoverExistingThumbnails(dir: Path, videoName: String): List<Path> {
        return Files.walk(dir, 1).filter { Files.isRegularFile(it) }.filter { file ->
            val matcher = rediscoverPattern.matcher(file.fileName.toString())
            matcher.matches() && matcher.group(1) == videoName
        }.collect(Collectors.toList())
    }
}
