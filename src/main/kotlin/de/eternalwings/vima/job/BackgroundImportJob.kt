package de.eternalwings.vima.job

import de.eternalwings.vima.process.VideoThumbnailCreator
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class BackgroundImportJob(private val thumbnailCreator: VideoThumbnailCreator, private val eventCallStep: EventCallStep) {

    fun execute(videoId: Int, videoPath: Path) {
        thumbnailCreator.createThumbnailsFor(videoPath, videoId)
        eventCallStep.execute(videoId)
    }

}
