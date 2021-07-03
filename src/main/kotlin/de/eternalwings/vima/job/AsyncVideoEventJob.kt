package de.eternalwings.vima.job

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.event.VideoUpdateEvent
import de.eternalwings.vima.plugin.AsyncVideoHandler
import de.eternalwings.vima.plugin.VideoContainer
import de.eternalwings.vima.repository.VideoRepository
import org.quartz.JobExecutionContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch

@Component
class AsyncVideoEventJob(private val videoRepository: VideoRepository, private val eventPublish: ApplicationEventPublisher,
                         private val transactionTemplate: TransactionTemplate) : QuartzJobBean() {
    override fun executeInternal(context: JobExecutionContext) {
        val video = context.mergedJobDataMap["video"] as VideoContainer
        val handler = context.mergedJobDataMap["handler"] as AsyncVideoHandler

        val latch = CountDownLatch(1)

        handler.call(video) {
            latch.countDown()
        }

        latch.await()

        transactionTemplate.execute {
            var changed = false
            val original = videoRepository.getOne(video.id)
            video.changed.forEach { changedMetadata ->
                val metadataValue = video.metadata[changedMetadata] as MetadataValue<Any>
                val originalMetadata = original.metadata!![changedMetadata] as MetadataValue<Any>
                changed = changed || originalMetadata.value != metadataValue.value
                originalMetadata.value = metadataValue.value
            }

            if (changed) {
                videoRepository.save(original)
                eventPublish.publishEvent(VideoUpdateEvent(this, video.id))
            }
        }
    }
}
