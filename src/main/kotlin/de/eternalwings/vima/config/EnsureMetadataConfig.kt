package de.eternalwings.vima.config

import de.eternalwings.vima.repository.MetadataContainerRepository
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.support.TransactionTemplate
import javax.annotation.PostConstruct

@Configuration
class EnsureMetadataConfig {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var metadataRepository: MetadataRepository

    @Autowired
    lateinit var transactionExecutor: TransactionTemplate

    @Autowired
    lateinit var metadataContainerRepository: MetadataContainerRepository

    @PostConstruct
    fun check() {
        transactionExecutor.execute {
            val allMetadata = metadataRepository.findAll()
            val metadataForVideoCount = metadataContainerRepository.getMetadataCountsForVideos()
            val videosWithMissingMetadata = metadataForVideoCount.filter { it.metadataCount < allMetadata.size }
            if (videosWithMissingMetadata.isNotEmpty()) {
                val videos = videoRepository.findAllById(videosWithMissingMetadata.map { it.videoId })
                videos.forEach { video ->
                    allMetadata.forEach { metadata ->
                        if (!video.hasMetadata(metadata)) {
                            video.addMetadataValue(metadata.toValue())
                        }
                    }
                }
                videoRepository.saveAll(videos)
            }
        }
    }

}
