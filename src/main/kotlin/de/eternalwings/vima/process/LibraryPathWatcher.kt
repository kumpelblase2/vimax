package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Library
import de.eternalwings.vima.event.LibraryCreateEvent
import de.eternalwings.vima.event.LibraryDeleteEvent
import de.eternalwings.vima.repository.LibraryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey

data class LibraryWatch(
        val libraryId: Int,
        val path: Path,
        val watchKey: WatchKey
)

@Component
@ConditionalOnProperty(name = ["watch-files"], havingValue = "true")
class LibraryPathWatcher(private val videoImporter: VideoImporter,
                         private val videoProcess: VideoProcess,
                         libraryRepository: LibraryRepository) {
    private var watches: List<LibraryWatch> = emptyList()
    private val watchService = FileSystems.getDefault().newWatchService()

    init {
        LOGGER.info("Monitoring libraries for file changes")
        libraryRepository.findAll().forEach { startWatching(it) }
    }

    fun startWatching(library: Library) {
        val path = Paths.get(library.path!!)
        val registration = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE)
        watches = watches + LibraryWatch(library.id!!, path, registration)
    }

    fun stopWatching(library: Library) {
        val pair = watches.find { it.libraryId == library.id } ?: return
        val key = pair.watchKey
        key.cancel()
        watches = watches - pair
    }

    @EventListener
    fun onLibraryCreate(libraryCreateEvent: LibraryCreateEvent) {
        startWatching(libraryCreateEvent.library)
    }

    @EventListener
    fun onLibraryDelete(libraryDeleteEvent: LibraryDeleteEvent) {
        stopWatching(libraryDeleteEvent.library)
    }

    @Scheduled(fixedRate = 1000)
    fun checkForChanges() {
        val copy = ArrayList(watches)
        for (watchPair in copy) {
            val watch = watchPair.watchKey
            val events = watch.pollEvents()
            for (event in events) {
                val file = event.context() as Path
                val absoluteFile = watchPair.path.resolve(file)
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    videoImporter.considerForImport(absoluteFile, watchPair.libraryId)
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    videoProcess.deleteVideoAt(absoluteFile)
                }
            }
        }
    }
    
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(LibraryPathWatcher::class.java)
    }
}
