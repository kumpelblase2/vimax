package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Video
import org.springframework.stereotype.Component
import java.nio.file.Files

@Component
class ThumbnailDeleter {
    fun deleteThumbnailsOf(videos: Collection<Video>) = videos.forEach { deleteThumbnailsOf(it) }

    fun deleteThumbnailsOf(video: Video) {
        video.thumbnails.forEach { thumbnail ->
            Files.deleteIfExists(thumbnail.locationPath)
        }
    }
}
