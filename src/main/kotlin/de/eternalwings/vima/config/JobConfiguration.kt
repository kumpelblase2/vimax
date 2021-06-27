package de.eternalwings.vima.config

import de.eternalwings.vima.job.BackgroundThumbnailJob
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class JobConfiguration {

    @Autowired
    private lateinit var scheduler: Scheduler

    @PostConstruct
    fun postConstruct() {
        val thumbnailJob = JobBuilder.newJob(BackgroundThumbnailJob::class.java)
            .withIdentity("missing-thumbnail-generate-job")
            .storeDurably()
            .build()

        val trigger = TriggerBuilder.newTrigger().forJob(thumbnailJob)
            .withIdentity("missing-thumbnail-generator-schedule")
            .withSchedule(SimpleScheduleBuilder.repeatHourlyForever())
            .startNow()
            .build()

        scheduler.scheduleJob(thumbnailJob, trigger)
    }

}
