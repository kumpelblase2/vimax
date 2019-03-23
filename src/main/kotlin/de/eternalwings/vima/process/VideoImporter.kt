package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class VideoImporter(val jobLauncher: JobLauncher, val importJob: Job) {

    private val maxImport = 10
    private var imported = 0

    @Async
    fun considerForImport(videoPath: Path, library: Library? = null) {
        if (imported >= maxImport) return
        imported += 1
        val parameterBuilder = JobParametersBuilder().addString("path", videoPath.toString())
        if(library != null) {
            parameterBuilder.addLong("library", library.id!!.toLong())
        }
        val parameters = parameterBuilder.toJobParameters()
        val execution = jobLauncher.run(importJob, parameters)
        LOGGER.debug("Started import job with id ${execution.jobId}")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VideoImporter::class.java)
    }
}
