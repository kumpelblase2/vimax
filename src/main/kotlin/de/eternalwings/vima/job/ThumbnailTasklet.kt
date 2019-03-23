package de.eternalwings.vima.job

import de.eternalwings.vima.process.VideoThumbnailCreator
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.RepeatStatus.FINISHED
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
class ThumbnailTasklet(private val videoThumbnailCreator: VideoThumbnailCreator) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val videoPath = getVideoPathFromContext(chunkContext)
        val videoId = getVideoIdFromContext(chunkContext)
        videoThumbnailCreator.createThumbnailsFor(videoPath, videoId)

        return FINISHED
    }

    fun getVideoPathFromContext(chunkContext: ChunkContext): Path {
        return Paths.get(chunkContext.stepContext.stepExecution.jobParameters.getString("path"))
    }

    fun getVideoIdFromContext(chunkContext: ChunkContext): Int {
        return chunkContext.stepContext.stepExecution.jobExecution.executionContext.getInt("videoId")
    }
}
