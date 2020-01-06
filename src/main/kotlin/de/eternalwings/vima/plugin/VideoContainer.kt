package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video

class VideoContainer(
        val id: Int,
        val name: String,
        val location: String,
        val metadata: Map<Int,MetadataValue<*>>
) {
    fun hasMetadata(id: Int) = metadata.containsKey(id)

    companion object {
        fun fromVideo(video: Video): VideoContainer {
            return VideoContainer(video.id!!, video.name!!, video.location!!, HashMap(video.metadata!!))
        }
    }
}
