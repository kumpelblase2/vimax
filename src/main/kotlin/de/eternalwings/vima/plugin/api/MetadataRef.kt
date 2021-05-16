package de.eternalwings.vima.plugin.api

import de.eternalwings.vima.domain.MetadataOptions
import de.eternalwings.vima.plugin.VideoContainer
import org.springframework.data.domain.Sort.Direction

sealed class MetadataRef<T>(val name: String) {
    private var internalId: Int? = null

    internal fun assignId(id: Int) {
        this.internalId = id
    }

    internal fun assignValueOn(container: VideoContainer, value: T?) {
        val id = this.internalId ?: throw IllegalStateException("No ID specified for metadata")
        container.updateMetadata(id, value)
    }

    internal fun getValueOf(container: VideoContainer): T? {
        val id = this.internalId ?: throw IllegalStateException("No ID specified for metadata")
        return container.getMetadataValue(id)
    }

    internal fun resetId() {
        this.internalId = null
    }

    class OwnedMetadataRef<T, S>(
        name: String,
        val ordering: Direction,
        val options: MetadataOptions<T, S>,
        val editable: Boolean = true
    ) :
        MetadataRef<S>(name) {
    }

    class SharedMetadataRef<T>(name: String) : MetadataRef<T>(name) {
    }
}
