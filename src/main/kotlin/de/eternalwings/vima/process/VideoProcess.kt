package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.event.VideoDeleteEvent
import de.eternalwings.vima.event.VideoUpdateEvent
import de.eternalwings.vima.plugin.EventType.UPDATE
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.persistence.EntityNotFoundException

@Component
class VideoProcess(private val videoRepository: VideoRepository,
                   private val thumbnailProcess: ThumbnailProcess,
                   private val thumbnailCreator: VideoThumbnailCreator,
                   private val pluginManager: PluginManager,
                   private val metadataRepository: MetadataRepository,
                   private val applicationEventPublisher: ApplicationEventPublisher) {

    @Transactional
    fun deleteAllVideosInLibrary(library: Library, deleteThumbnails: Boolean = false) {
        val videosToDelete = videoRepository.findByLibrary(library)
        if (deleteThumbnails) {
            thumbnailProcess.deleteThumbnailsOf(videosToDelete)
        }
        videoRepository.deleteAll(videosToDelete)
    }

    @Transactional
    fun refreshThumbnailsFor(video: Video) {
        thumbnailProcess.deleteThumbnailsOf(video)
        video.thumbnails.clear()
        videoRepository.save(video)
        thumbnailCreator.createThumbnailsFor(Paths.get(video.location!!), video.id!!)
    }

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

    @Transactional
    fun updateVideoFromUser(video: Video) : Video {
        val existing = videoRepository.findById(video.id!!).orElseThrow { EntityNotFoundException() }
        val metadata = metadataRepository.findAll()
        this.assignNewValuesToVideo(existing, video, metadata)

        pluginManager.callEvent(UPDATE, existing)

        return videoRepository.save(existing)
    }

    @Transactional
    fun updateVideosFromUser(videos: List<Video>) : List<Video> {
        val existingVideos = videoRepository.findAllById(videos.map { it.id!! })
        val metadata = metadataRepository.findAll()
        existingVideos.forEach { old ->
            val updated = videos.find { it.id == old.id } ?: return@forEach
            assignNewValuesToVideo(old, updated, metadata)
        }

        pluginManager.callEvent(UPDATE, existingVideos)

        return videoRepository.saveAll(existingVideos)
    }

    fun resetValuesToDefault(metadataId: Int, value: Any) {
        val videos = videoRepository.findVideosWithMetadataValue(metadataId, value)
        videoRepository.assignDefaultIfValueForMetadataIs(metadataId, value)
        videos.forEach {
            applicationEventPublisher.publishEvent(VideoUpdateEvent(this, it))
        }
    }

    private fun assignNewValuesToVideo(existing: Video, newVideo: Video, metadata: List<Metadata>) {
        existing.selectedThumbnail = newVideo.selectedThumbnail
        existing.name = newVideo.name
        newVideo.metadata?.forEach { (metadataId, value) ->
            val definition = metadata.find { it.id == metadataId } ?: return@forEach
            if (!definition.readOnly) { // Only update non-system metadata
                val existingValue = existing.metadata?.get(metadataId) ?: throw IllegalStateException("Missing metadata")
                @Suppress("UNCHECKED_CAST") // Can't do it without the cast :(
                (existingValue as MetadataValue<Any>).value = value.value
            }
        }

        existing.metadata!!.entries.removeIf { entry -> metadata.none { it.id == entry.key } }
    }
}
