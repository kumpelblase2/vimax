package de.eternalwings.vima.config

import de.eternalwings.vima.process.VideoProcess
import de.eternalwings.vima.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PostConstruct

@ConditionalOnProperty("delete-missing-videos", matchIfMissing = true)
@Configuration
class CheckDeletedFiles {
    @Autowired
    lateinit var videoProcess: VideoProcess

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var txTemplate: TransactionTemplate

    @PostConstruct
    fun checkMissingVideos() {
        txTemplate.execute {
            videoRepository.findAll().stream().filter {
                val path = Paths.get(it.location!!)
                val container = path.parent
                if (!Files.exists(container)) {
                    false
                } else {
                    !Files.exists(Paths.get(it.location!!))
                }
            }.forEach {
                LOGGER.warn("Video ${it.name} no longer exists on the file system, removing.")
                videoProcess.deleteVideo(it)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CheckDeletedFiles::class.java)
    }
}
