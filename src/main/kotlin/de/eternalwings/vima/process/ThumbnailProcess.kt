package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Thumbnail
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.repository.ThumbnailRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import javax.persistence.EntityManager

@Component
class ThumbnailProcess(private val thumbnailRepository: ThumbnailRepository, private val entityManager: EntityManager) {

    @Transactional
    fun addThumbnailsToVideo(videoId: Int, thumbnails: Iterable<String>) {
        thumbnails.forEach { thumb ->
            val video = entityManager.getReference(Video::class.java, videoId)
            val thumbnail = Thumbnail(location = thumb, video = video)
            thumbnailRepository.save(thumbnail)
        }
        thumbnailRepository.flush()
    }

    fun deleteThumbnailsOf(videos: Collection<Video>) = videos.forEach { deleteThumbnailsOf(it) }

    fun deleteThumbnailsOf(video: Video) {
        video.thumbnails.forEach { thumbnail ->
            Files.deleteIfExists(thumbnail.locationPath)
        }
    }

}
