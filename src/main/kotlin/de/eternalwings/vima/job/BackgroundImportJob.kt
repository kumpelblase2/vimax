package de.eternalwings.vima.job

import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class BackgroundImportJob(private val thumbnailStep: ThumbnailStep, private val eventCallStep: EventCallStep) {

    fun execute(videoId: Int, videoPath: Path) {
        thumbnailStep.execute(videoId, videoPath)
        eventCallStep.execute(videoId)
    }

}
