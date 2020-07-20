package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

@Component
class VideoImporter(val jobLauncher: JobLauncher, val importJob: Job) {

    @Async
    fun considerForImport(videoPath: Path, library: Library): CompletableFuture<Unit> {
        return considerForImport(videoPath, library.id!!)
    }

    @Async
    fun considerForImport(videoPath: Path, libraryId: Int): CompletableFuture<Unit> {
        if (!isVideoFile(videoPath)) return CompletableFuture.completedFuture(null)
        val parameters = JobParametersBuilder().addString("path", videoPath.toString())
            .addLong("library", libraryId.toLong())
            .addLong("time", System.currentTimeMillis()).toJobParameters()
        val execution = jobLauncher.run(importJob, parameters)
        LOGGER.debug("Started import job with id ${execution.jobId}")
        return CompletableFuture.completedFuture(null)
    }

    private fun isVideoFile(possibleVideoFile: Path): Boolean {
        return Files.isRegularFile(possibleVideoFile) && possibleVideoFile.fileName.toString().endsWith(".mp4")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VideoImporter::class.java)
    }
}
