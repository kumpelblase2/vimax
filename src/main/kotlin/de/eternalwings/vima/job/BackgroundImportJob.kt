package de.eternalwings.vima.job

import de.eternalwings.vima.process.VideoThumbnailCreator
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class BackgroundImportJob(private val thumbnailCreator: VideoThumbnailCreator, private val eventCallStep: EventCallStep) :
    QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
        val dataMap = context.jobDetail.jobDataMap
        val videoId = dataMap.getInt("id")
        val videoPath = dataMap["path"] as Path
        thumbnailCreator.createThumbnailsFor(videoPath, videoId)
        eventCallStep.execute(videoId)
    }

}
