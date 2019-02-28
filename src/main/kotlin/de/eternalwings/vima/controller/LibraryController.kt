package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.process.VideoLoader
import de.eternalwings.vima.repository.LibraryRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths
import javax.persistence.EntityNotFoundException

@RestController
@RequestMapping("/api/library")
class LibraryController(private val libraryRepository: LibraryRepository,
                        private val videoLoader: VideoLoader) {

    @PostMapping
    fun saveLibrary(@RequestBody library: Library): Library {
        val path = Paths.get(library.path)
        if (!path.isAbsolute) {
            throw IllegalArgumentException("Isn't absolute.")
        }

        if (!Files.isReadable(path)) {
            throw IllegalArgumentException("Not readable.")
        }

        if(!Files.isDirectory(path)) {
            throw IllegalArgumentException("Not a directory.")
        }

        if (library.id != null) {
            throw IllegalStateException("Cannot alter existing library.")
        } else {
            val existing = libraryRepository.findByPath(library.path!!)
            if (existing == null) {
                val newLibrary = libraryRepository.save(library)
                videoLoader.scanDirectory(path)
                return newLibrary
            } else {
                throw IllegalStateException("Already exists.")
            }
        }
    }

    @GetMapping
    fun getLibraries(): List<Library> = libraryRepository.findAll()

    @DeleteMapping("/{id}")
    fun deleteLibrary(@PathVariable("id") id: Int): Int {
        libraryRepository.deleteById(id)
        return id
    }

    @GetMapping("/scan")
    fun scanLibrary() {
        videoLoader.scan()
    }

    @GetMapping("/scan/{id}")
    fun scanDirectory(@PathVariable("id") libraryId: Int) {
        val library = libraryRepository.findById(libraryId).orElseThrow { EntityNotFoundException() }
        videoLoader.scanDirectory(Paths.get(library.path))
    }

}
