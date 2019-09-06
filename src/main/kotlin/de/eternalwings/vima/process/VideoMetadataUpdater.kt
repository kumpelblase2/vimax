package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class VideoMetadataUpdater(private val videoRepository: VideoRepository) {
    @Transactional
    fun addMetadata(metadata: Metadata) {
        val allVideos = videoRepository.findAll()
        allVideos.forEach { video ->
            if (!video.hasMetadata(metadata)) {
                video.addMetadataValue(metadata.toValue())
            }
        }
        videoRepository.saveAll(allVideos)
    }
}
