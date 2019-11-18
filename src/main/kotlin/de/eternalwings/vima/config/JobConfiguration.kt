package de.eternalwings.vima.config

import de.eternalwings.vima.job.EventCallTasklet
import de.eternalwings.vima.job.LoadVideoTasklet
import de.eternalwings.vima.job.ThumbnailTasklet
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors

@Configuration
@EnableAsync
@EnableBatchProcessing
class JobConfiguration {
    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(Executors.newFixedThreadPool(5))
    }

    @Bean
    fun importJob(importStep: Step, analyzeStep: Step, eventCallStep: Step): Job {
        return jobBuilderFactory.get("import-videos").incrementer(RunIdIncrementer())
            .flow(importStep).next(analyzeStep).next(eventCallStep).end()
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
    fun eventCallStep(eventCallTasklet: EventCallTasklet): Step {
        return stepBuilderFactory.get("event-call-step").tasklet(eventCallTasklet).build()
    }
}
