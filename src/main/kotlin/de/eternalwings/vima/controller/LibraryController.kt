package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.event.LibraryCreateEvent
import de.eternalwings.vima.event.LibraryDeleteEvent
import de.eternalwings.vima.process.VideoLoader
import de.eternalwings.vima.process.VideoProcess
import de.eternalwings.vima.repository.LibraryRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths
import javax.persistence.EntityNotFoundException

@RestController
@RequestMapping("/api/library")
class LibraryController(private val libraryRepository: LibraryRepository,
                        private val videoLoader: VideoLoader,
                        private val videoProcess: VideoProcess,
                        private val eventPublisher: ApplicationEventPublisher) {

    @Transactional
    @PostMapping
    fun saveLibrary(@RequestBody library: Library): Library {
        val path = Paths.get(library.path!!)
        if (!path.isAbsolute) {
            throw IllegalArgumentException("Path isn't absolute.")
        }

        if (!Files.isReadable(path)) {
            throw IllegalArgumentException("Path isn't readable.")
        }

        if (!Files.isDirectory(path)) {
            throw IllegalArgumentException("Path is not a directory.")
        }

        if (library.id != null) {
            throw IllegalStateException("Cannot alter existing library.")
        } else {
            val existing = libraryRepository.findByPath(library.path!!)
            if (existing == null) {
                val newLibrary = libraryRepository.save(library)
                videoLoader.scanLibrary(newLibrary)
                eventPublisher.publishEvent(LibraryCreateEvent(this, newLibrary))
                return newLibrary
            } else {
                throw IllegalStateException("Library for the given path already exists.")
            }
        }
    }

    @Transactional(readOnly = true)
    @GetMapping
    fun getLibraries(): List<Library> = libraryRepository.findAll()

    @Transactional
    @DeleteMapping("/{id}")
    fun deleteLibrary(@PathVariable("id") id: Int,
                      @RequestParam("delete_thumbnails", required = false, defaultValue = "false")
                      deleteThumbnails: Boolean): Int {
        val library = libraryRepository.getOne(id)
        videoProcess.deleteAllVideosInLibrary(library, deleteThumbnails)
        eventPublisher.publishEvent(LibraryDeleteEvent(this, library))
        libraryRepository.delete(library)
        return id
    }

    @GetMapping("/scan")
    fun scanLibrary() {
        videoLoader.scanAllLibraries()
    }

    @Transactional(readOnly = true)
    @GetMapping("/scan/{id}")
    fun scanDirectory(@PathVariable("id") libraryId: Int) {
        val library = libraryRepository.findById(libraryId).orElseThrow { EntityNotFoundException() }
        videoLoader.scanLibrary(library)
    }

}
