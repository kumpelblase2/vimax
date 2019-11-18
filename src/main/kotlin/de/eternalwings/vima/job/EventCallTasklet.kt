package de.eternalwings.vima.job

import de.eternalwings.vima.plugin.EventType.CREATE
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.RepeatStatus.FINISHED
import org.springframework.stereotype.Component

@Component
class EventCallTasklet(private val videoRepository: VideoRepository) : Tasklet {
    override fun execute(contribution: StepContribution, context: ChunkContext): RepeatStatus? {
        val video = videoRepository.getOne(getVideoIdFromContext(context))
        PluginManager.callEvent(CREATE, video)
        return FINISHED
    }

    fun getVideoIdFromContext(chunkContext: ChunkContext): Int {
        return chunkContext.stepContext.stepExecution.jobExecution.executionContext.getInt("videoId")
    }
}
