package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

@Component
class VideoImporter(val jobLauncher: JobLauncher, val importJob: Job) {

    private val maxImport = 10
    private var imported = 0

    @Async
    fun considerForImport(videoPath: Path, library: Library): CompletableFuture<Unit> {
        return considerForImport(videoPath, library.id!!)
    }

    @Async
    fun considerForImport(videoPath: Path, libraryId: Int): CompletableFuture<Unit> {
        if (imported >= maxImport) return CompletableFuture.failedFuture(IllegalStateException("Reached import limit."))
        imported += 1
        val parameters = JobParametersBuilder().addString("path", videoPath.toString())
                .addLong("library", libraryId.toLong()).toJobParameters()
        val execution = jobLauncher.run(importJob, parameters)
        LOGGER.debug("Started import job with id ${execution.jobId}")
        return CompletableFuture.completedFuture(null)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VideoImporter::class.java)
    }
}
