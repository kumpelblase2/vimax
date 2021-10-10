package de.eternalwings.vima.job

import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.repository.LibraryRepository
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class LoadVideoStep(private val videoRepository: VideoRepository,
                    private val libraryRepository: LibraryRepository,
                    private val metadataRepository: MetadataRepository) {
    fun execute(location: Path, libraryId: Int): Int {
        val filename = location.fileName.toString()
        val library = libraryRepository.getById(libraryId)
        val nameWithoutExtension = getNameWithoutExtension(filename)
        val video = Video(location = location.toString(), name = nameWithoutExtension, library = library)
        appendMetadataTo(video)
        val saved = videoRepository.save(video)
        return saved.id!!
    }

    private fun appendMetadataTo(video: Video) {
        metadataRepository.findAll().forEach { video.addMetadataValue(it.id!!, it.toValue()) }
    }

    fun getNameWithoutExtension(filename: String) = filename.substringBeforeLast(".")
}
