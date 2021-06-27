package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.job.BackgroundJobController
import de.eternalwings.vima.job.LoadVideoStep
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

@Component
class VideoImporter(
    private val backgroundJobController: BackgroundJobController,
    private val loadStep: LoadVideoStep
) {

    fun considerForImport(videoPath: Path, library: Library) {
        return considerForImport(videoPath, library.id!!)
    }

    fun considerForImport(videoPath: Path, libraryId: Int) {
        if (!isVideoFile(videoPath)) return
        val videoId = loadStep.execute(videoPath, libraryId)
        backgroundJobController.scheduleBackgroundImport(videoId, videoPath)
        LOGGER.debug("Started import job for video at $videoPath")
    }

    private fun isVideoFile(possibleVideoFile: Path): Boolean {
        return Files.isRegularFile(possibleVideoFile) && possibleVideoFile.fileName.toString().endsWith(".mp4")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VideoImporter::class.java)
    }
}
