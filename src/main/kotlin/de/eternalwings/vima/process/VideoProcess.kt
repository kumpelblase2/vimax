package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.query.VideoSearcher
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.stereotype.Component
import java.nio.file.Paths
import javax.transaction.Transactional

@Component
class VideoProcess(private val videoRepository: VideoRepository,
                   private val thumbnailDeleter: ThumbnailDeleter,
                   private val thumbnailCreator: VideoThumbnailCreator,
                   private val videoSearcher: VideoSearcher) {
    fun deleteAllVideosInLibrary(library: Library) {
        val videosToDelete = videoRepository.findByLibrary(library)
        thumbnailDeleter.deleteThumbnailsOf(videosToDelete)
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
}
