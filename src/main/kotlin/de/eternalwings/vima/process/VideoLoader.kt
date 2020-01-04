package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
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
        scanAllLibraries()
    }

    fun scanAllLibraries() = scanLibraries(libraryRepository.findAll())

    fun scanLibraries(libraries: List<Library>) {
        val existingVideos = videoRepository.findAll()
        val existingPaths = existingVideos.map { Paths.get(it.location!!) }.toSet()
        libraries.forEach { library ->
            Files.walk(Paths.get(library.path!!), 1, FOLLOW_LINKS)
                .filter { !existingPaths.contains(it) }
                .forEach { videoImporter.considerForImport(it, library) }
        }
    }

    fun scanLibrary(library: Library) {
        scanLibraries(listOf(library))
    }

}
