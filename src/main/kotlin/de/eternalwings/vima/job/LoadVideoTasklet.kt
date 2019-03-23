package de.eternalwings.vima.job

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.repository.LibraryRepository
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.RepeatStatus.FINISHED
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
class LoadVideoTasklet(private val videoRepository: VideoRepository,
                       private val libraryRepository: LibraryRepository,
                       private val metadataRepository: MetadataRepository) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val videoPathFromContext = getVideoPathFromContext(chunkContext)
        val filename = videoPathFromContext.fileName.toString()
        val library = getLibrary(chunkContext)
        val nameWithoutExtension = getNameWithoutExtension(filename)
        val video = Video(location = videoPathFromContext.toString(), name = nameWithoutExtension,
                library = library)
        appendMetadataTo(video)
        val saved = videoRepository.save(video)
        setVideoIdInContext(chunkContext, saved.id!!)
        return FINISHED
    }

    private fun appendMetadataTo(video: Video) {
        metadataRepository.findAll().map { it.toValue() }.forEach { video.addMetadataValue(it) }
    }

    fun getVideoPathFromContext(chunkContext: ChunkContext): Path {
        return Paths.get(chunkContext.stepContext.stepExecution.jobParameters.getString("path"))
    }

    fun setVideoIdInContext(chunkContext: ChunkContext, videoId: Int) {
        chunkContext.stepContext.stepExecution.jobExecution.executionContext.putInt("videoId", videoId)
    }

    fun getLibrary(chunkContext: ChunkContext): Library? {
        val libraryId = chunkContext.stepContext.stepExecution.jobParameters.getLong("library")
        return libraryRepository.getOne(libraryId.toInt())
    }

    fun getNameWithoutExtension(filename: String) = filename.substringBeforeLast(".")
}
