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

    fun <T> updateMetadata(id: Int, value: T?) {
        val metadata = this.metadata[id] ?: throw IllegalStateException("No such metadata with id $id")
        (metadata as MetadataValue<T>).value = value
        markChanged(id)
    }

    fun <T> getMetadataValue(id: Int): T? {
        val metadata = this.metadata[id] ?: throw IllegalStateException("No such metadata with id $id")
        return (metadata as MetadataValue<T>).value
    }

    companion object {
        fun fromVideo(video: Video): VideoContainer {
            return VideoContainer(video.id!!, video.name!!, video.location!!, HashMap(video.metadata!!))
        }
    }
}
