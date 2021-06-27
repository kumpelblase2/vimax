package de.eternalwings.vima.job

import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class BackgroundJobController(private val scheduler: Scheduler) {

    private fun createNowTrigger() = TriggerBuilder.newTrigger().startNow().build()

    fun scheduleBackgroundImport(videoId: Int, videoPath: Path) {
        val jobDetail = buildBackgroundImportJobDetail(videoId, videoPath)
        val trigger = createNowTrigger()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    private fun buildBackgroundImportJobDetail(videoId: Int, videoPath: Path): JobDetail {
        val dataMap = JobDataMap(mapOf(
            "id" to videoId,
            "path" to videoPath
        ))

        return JobBuilder.newJob(BackgroundImportJob::class.java)
            .withIdentity(videoId.toString(), "background-import")
            .usingJobData(dataMap)
            .build()
    }

    fun scheduleThumbnailJob(videoId: Int, videoPath: Path) {
        val jobDetail = buildBackgroundThumbnailJobDetail(videoId, videoPath)
        val trigger = createNowTrigger()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    private fun buildBackgroundThumbnailJobDetail(videoId: Int, videoPath: Path): JobDetail {
        val dataMap = JobDataMap(mapOf(
            "id" to videoId,
            "path" to videoPath
        ))

        return JobBuilder.newJob(ThumbnailGeneratorJob::class.java)
            .withIdentity(videoId.toString(), "thumbnail-generation")
            .usingJobData(dataMap)
            .build()
    }

}
