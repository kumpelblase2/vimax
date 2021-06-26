package de.eternalwings.vima.dto

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video

data class VideoDTO(
    val id: Int,
    val name: String,
    val library: Int,
    val metadata: Map<Int, MetadataValue<*>>,
    val thumbnails: List<Int>,
    val selectedThumbnail: Int
) {
    companion object {
        fun fromVideo(video: Video): VideoDTO {
            return VideoDTO(
                video.id!!,
                video.name!!,
                video.library?.id!!,
                video.metadata!!,
                video.thumbnails.map { it.id!! },
                video.selectedThumbnail!!
            )
        }
    }
}
