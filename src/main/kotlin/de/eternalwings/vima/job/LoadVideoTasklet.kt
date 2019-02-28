package de.eternalwings.vima.job

import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.RepeatStatus.FINISHED
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
class LoadVideoTasklet(val videoRepository: VideoRepository) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val videoPathFromContext = getVideoPathFromContext(chunkContext)
        val filename = videoPathFromContext.fileName.toString()
        val video =
                Video(location = videoPathFromContext.toString(), name = filename.substringBeforeLast("."))
        videoRepository.save(video)
        return FINISHED
    }

    fun getVideoPathFromContext(chunkContext: ChunkContext): Path {
        return Paths.get(chunkContext.stepContext.stepExecution.jobParameters.getString("path"))
    }
}
