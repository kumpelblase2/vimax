package de.eternalwings.vima.job

import de.eternalwings.vima.domain.Thumbnail
import de.eternalwings.vima.process.ThumbnailGenerator
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.RepeatStatus.FINISHED
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import java.util.stream.Collectors

@Component
class ThumbnailTasklet(private val thumbnailGenerator: ThumbnailGenerator,
                       private val videoRepository: VideoRepository,
                       @Value("\${thumbnail-amount:3}") private val thumbnailCount: Int = 3) : Tasklet {

    val rediscoverPattern: Pattern = Pattern.compile("(.+)_\\d+\\.jpg")

    private val relativeThumbnailDir: Path = Paths.get(".thumbnails-new")

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val videoPath = getVideoPathFromContext(chunkContext)
        val parentPath = videoPath.parent
        val thumbnailDir = parentPath.resolve(relativeThumbnailDir)
        val video = videoRepository.findByLocation(videoPath.toString()) ?: throw IllegalStateException()
        val existingThumbnails =
                this.discoverExistingThumbnails(thumbnailDir, videoPath.fileName.toString()
                    .substringBeforeLast("."))
        val remainingThumbnails = Math.max(thumbnailCount - existingThumbnails.size, 0)
        val thumbnails =
                thumbnailGenerator.generateThumbnailsFor(videoPath, thumbnailDir, remainingThumbnails)
        video.thumbnails = (existingThumbnails + thumbnails).map { Thumbnail(location = it.toString()) }
            .toMutableList()
        videoRepository.save(video)
        return FINISHED
    }

    fun discoverExistingThumbnails(dir: Path, videoName: String): List<Path> {
        return Files.walk(dir, 1).filter { Files.isRegularFile(it) }.filter { file ->
            val matcher = rediscoverPattern.matcher(file.fileName.toString())
            matcher.matches() && matcher.group(1) == videoName
        }.collect(Collectors.toList())
    }

    fun getVideoPathFromContext(chunkContext: ChunkContext): Path {
        return Paths.get(chunkContext.stepContext.stepExecution.jobParameters.getString("path"))
    }
}
