package de.eternalwings.vima.job

import de.eternalwings.vima.config.ThumbnailSettings
import de.eternalwings.vima.repository.LibraryRepository
import de.eternalwings.vima.repository.ThumbnailRepository
import de.eternalwings.vima.repository.VideoRepository
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

@Component
class BackgroundThumbnailJob(
    private val videoRepository: VideoRepository,
    private val thumbnailRepository: ThumbnailRepository,
    private val libraryRepository: LibraryRepository,
    private val backgroundJobController: BackgroundJobController,
    private val thumbnailSettings: ThumbnailSettings
) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
        generateThumbnailsForVideosWithout()
        if (thumbnailSettings.generateMissing) {
            generateRemainingThumbnails()
        }
        if (thumbnailSettings.removeLingering) {
            removeUnusedThumbnails()
        }
    }

    private fun generateRemainingThumbnails() {
        val videosWithLessThumbnails =
            videoRepository.findVideosWithThumbnailCountNotMatching(thumbnailSettings.count)
                .map { it.id!! to Paths.get(it.location!!) }
        videosWithLessThumbnails.map {
            LOGGER.info("Generating more thumbnails for video id ${it.first}")
            backgroundJobController.scheduleThumbnailJob(it.first, it.second)
        }
    }

    private fun generateThumbnailsForVideosWithout() {
        val videosWithoutThumbnails =
            videoRepository.findVideosWithMissingThumbnails().map { it.id!! to Paths.get(it.location!!) }
        videosWithoutThumbnails.map {
            LOGGER.info("Regenerating thumbnails for video id ${it.first}")
            backgroundJobController.scheduleThumbnailJob(it.first, it.second)
        }
    }

    private fun removeUnusedThumbnails() {
        val thumbnails = thumbnailRepository.getAllLocations().map { Paths.get(it) }.toSet()
        val libraries = libraryRepository.findAll().mapNotNull { it.path }
        libraries.forEach { library ->
            val thumbnailDir = Paths.get(library).resolve(thumbnailSettings.relativePath)
            val filesToRemove = Files.list(thumbnailDir).filter { isThumbnail(it) }.filter { !thumbnails.contains(it) }
                .collect(Collectors.toSet())
            filesToRemove.forEach {
                Files.delete(it)
                LOGGER.info("Removing lingering file: $it")
            }
        }
    }

    private fun isThumbnail(file: Path): Boolean {
        return Files.isRegularFile(file) && !Files.isHidden(file)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BackgroundThumbnailJob::class.java)
    }
}
