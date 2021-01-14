package de.eternalwings.vima.config

import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.storage.InMemoryStorageProvider
import org.jobrunr.storage.StorageProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration {

    @Bean
    fun storageProvider(jobMapper: JobMapper): StorageProvider {
        val storageProvider = InMemoryStorageProvider()
        storageProvider.setJobMapper(jobMapper)
        return storageProvider
    }
}
