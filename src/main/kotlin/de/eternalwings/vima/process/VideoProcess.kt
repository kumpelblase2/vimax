package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.event.VideoCreateEvent
import de.eternalwings.vima.event.VideoDeleteEvent
import de.eternalwings.vima.query.VideoSearcher
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.transaction.Transactional

@Component
class VideoProcess(private val videoRepository: VideoRepository,
                   private val thumbnailDeleter: ThumbnailDeleter,
                   private val thumbnailCreator: VideoThumbnailCreator,
                   private val videoSearcher: VideoSearcher,
                   private val applicationEventPublisher: ApplicationEventPublisher) {
    fun deleteAllVideosInLibrary(library: Library, deleteThumbnails: Boolean = false) {
        val videosToDelete = videoRepository.findByLibrary(library)
        if (deleteThumbnails) {
            thumbnailDeleter.deleteThumbnailsOf(videosToDelete)
        }
        videoRepository.deleteAll(videosToDelete)
    }

    @Transactional
    fun refreshThumbnailsFor(video: Video) {
        thumbnailDeleter.deleteThumbnailsOf(video)
        video.thumbnails.clear()
        videoRepository.save(video)
        thumbnailCreator.createThumbnailsFor(Paths.get(video.location!!), video.id!!)
    }

    fun searchFor(query: String) = videoSearcher.search(query)

    @Transactional
    fun deleteVideoAt(path: Path) {
        val video = videoRepository.findVideoByLocation(path.toString()) ?: return
        deleteVideo(video)
    }

    @Transactional
    fun deleteVideo(video: Video) {
        video.thumbnails.forEach { thumbnail ->
            Files.delete(thumbnail.locationPath)
        }
        videoRepository.delete(video)
        applicationEventPublisher.publishEvent(VideoDeleteEvent(this, video))
    }
}
