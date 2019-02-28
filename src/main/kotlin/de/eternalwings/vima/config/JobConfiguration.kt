package de.eternalwings.vima.config

import de.eternalwings.vima.job.ThumbnailTasklet
import de.eternalwings.vima.job.LoadVideoTasklet
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableBatchProcessing
class JobConfiguration {
    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    @Bean
    fun importJob(importStep: Step, analyzeStep: Step): Job {
        return jobBuilderFactory.get("import-videos").incrementer(RunIdIncrementer())
            .flow(importStep).next(analyzeStep).end()
            .build()
    }

    @Bean
    fun importStep(loadVideoTasklet: LoadVideoTasklet): Step {
        return stepBuilderFactory.get("video-import-step").tasklet(loadVideoTasklet).build()
    }

    @Bean
    fun analyzeStep(thumbnailTasklet: ThumbnailTasklet): Step {
        return stepBuilderFactory.get("video-thumbnail-step").tasklet(thumbnailTasklet).build()
    }

    @Bean
    fun taskExecutor(): TaskExecutor = ThreadPoolTaskExecutor()

    @Bean
    fun jobLauncher(jobRepository: JobRepository, taskExecutor: TaskExecutor): JobLauncher {
        val launcher = SimpleJobLauncher()
        launcher.setJobRepository(jobRepository)
        launcher.setTaskExecutor(taskExecutor)
        return launcher
    }
}
