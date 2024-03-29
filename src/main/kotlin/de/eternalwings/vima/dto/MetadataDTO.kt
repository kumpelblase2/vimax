package de.eternalwings.vima.dto

import de.eternalwings.vima.MetadataType
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.MetadataOptions
import de.eternalwings.vima.domain.PluginInformation
import org.springframework.data.domain.Sort.Direction

data class MetadataDTO(
    val id: Int?,
    val name: String,
    val type: MetadataType,
    val ordering: Direction,
    val readOnly: Boolean,
    val ownerId: Int?,
    val options: MetadataOptions<*,*>,
    val displayOrder: Int,
    val version: Int?
) {
    companion object {
        fun fromMetadata(metadata: Metadata): MetadataDTO {
            return MetadataDTO(
                metadata.id,
                metadata.name!!,
                metadata.type!!,
                metadata.ordering,
                metadata.readOnly,
                metadata.owner?.id,
                metadata.options!!,
                metadata.displayOrder,
                metadata.version
            )
        }
    }
}
