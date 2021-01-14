package de.eternalwings.vima.job

import de.eternalwings.vima.process.VideoThumbnailCreator
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class ThumbnailStep(private val videoThumbnailCreator: VideoThumbnailCreator) {

    fun execute(videoId: Int, videoLocation: Path) {
        videoThumbnailCreator.createThumbnailsFor(videoLocation, videoId)
    }
}
