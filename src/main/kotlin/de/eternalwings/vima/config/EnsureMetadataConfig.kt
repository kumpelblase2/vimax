package de.eternalwings.vima.config

import de.eternalwings.vima.process.VideoProcess
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.support.TransactionTemplate
import javax.annotation.PostConstruct

@Configuration
class EnsureMetadataConfig {

    @Autowired
    lateinit var videoProcess: VideoProcess

    @Autowired
    lateinit var metadataRepository: MetadataRepository

    @Autowired
    lateinit var transactionExecutor: TransactionTemplate

    @PostConstruct
    fun check() {
        val allMetadata = metadataRepository.getAllIds()

        transactionExecutor.execute {
            allMetadata.forEach {
                videoProcess.addNewMetadataToVideos(it)
            }
        }
    }

}
