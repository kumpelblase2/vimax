package de.eternalwings.vima.process

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class VideoImporter(val jobLauncher: JobLauncher, val importJob: Job) {

    private val maxImport = 10
    private var imported = 0

    fun considerForImport(videoPath: Path) {
        if (imported >= maxImport) return
        imported += 1
        val parameters = JobParametersBuilder().addString("path", videoPath.toString()).toJobParameters()
        val execution = jobLauncher.run(importJob, parameters)
        LOGGER.debug("Started import job with id ${execution.jobId}")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VideoImporter::class.java)
    }
}
