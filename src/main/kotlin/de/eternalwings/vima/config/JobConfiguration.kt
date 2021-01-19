package de.eternalwings.vima.config

import de.eternalwings.vima.process.BackgroundThumbnailJobWrapper
import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.scheduling.cron.Cron
import org.jobrunr.storage.InMemoryStorageProvider
import org.jobrunr.storage.StorageProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class JobConfiguration {

    @Bean
    fun storageProvider(jobMapper: JobMapper): StorageProvider {
        val storageProvider = InMemoryStorageProvider()
        storageProvider.setJobMapper(jobMapper)
        return storageProvider
    }

    @Autowired
    private lateinit var jobScheduler: JobScheduler

    @PostConstruct
    fun setupThumbnailCleanup() {
        BackgroundThumbnailJobWrapper.scheduleThumbnailCleanupJob(jobScheduler, Cron.hourly())
    }
}
