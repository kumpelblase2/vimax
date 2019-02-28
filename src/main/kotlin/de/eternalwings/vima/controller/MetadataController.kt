package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MetadataController(val metadataRepository: MetadataRepository) {
    @GetMapping("/metadata")
    fun getAll(): List<Metadata> = metadataRepository.findAll()

    @PostMapping("/metadata")
    fun createMetadata(@RequestBody metadata: Metadata): Metadata {
        return this.metadataRepository.save(metadata)
    }

    @DeleteMapping("/metadata/{id}")
    fun deleteMetadata(@PathVariable("id") metadataId: Int): Int {
        this.metadataRepository.deleteById(metadataId)
        return metadataId
    }
}
