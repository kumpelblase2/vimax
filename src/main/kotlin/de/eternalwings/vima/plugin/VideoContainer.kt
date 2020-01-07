package de.eternalwings.vima.plugin

import de.eternalwings.vima.domain.MetadataValue
import de.eternalwings.vima.domain.Video

class VideoContainer(
        val id: Int,
        val name: String,
        val location: String,
        val metadata: Map<Int,MetadataValue<*>>
) {
    var changed: Set<Int> = emptySet()
        private set

    fun hasMetadata(id: Int) = metadata.containsKey(id)

    fun markChanged(metadataId: Int) {
        changed = changed + metadataId
    }

    companion object {
        fun fromVideo(video: Video): VideoContainer {
            return VideoContainer(video.id!!, video.name!!, video.location!!, HashMap(video.metadata!!))
        }
    }
}
