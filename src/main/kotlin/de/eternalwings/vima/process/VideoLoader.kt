package de.eternalwings.vima.process

import de.eternalwings.vima.repository.LibraryRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import java.nio.file.FileVisitOption.FOLLOW_LINKS
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class VideoLoader(
        val libraryRepository: LibraryRepository,
        val videoRepository: VideoRepository,
        val videoImporter: VideoImporter
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        scan()
    }

    fun scan() = scanDirectories(libraryRepository.findAll().map { Paths.get(it.path) })

    fun scanDirectories(paths: List<Path>) {
        val existingVideos = videoRepository.findAll()
        val existingPaths = existingVideos.map { Paths.get(it.location) }.toSet()
        paths.stream().flatMap { Files.walk(it, 1, FOLLOW_LINKS) }
            .filter { Files.isRegularFile(it) }
            .filter { isVideoFile(it) }
            .filter { !existingPaths.contains(it) }
            .forEach(videoImporter::considerForImport)
    }

    fun scanDirectory(path: Path) {
        scanDirectories(listOf(path))
    }

    private fun isVideoFile(possibleVideoFile: Path): Boolean {
        return possibleVideoFile.fileName.toString().endsWith(".mp4")
    }

}
