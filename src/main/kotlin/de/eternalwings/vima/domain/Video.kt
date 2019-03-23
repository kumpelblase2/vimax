package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.nio.file.Path
import java.nio.file.Paths
import javax.persistence.CascadeType.ALL
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType.LAZY
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Transient

@Entity
data class Video(
        @field:Column(updatable = false)
        var location: String? = null,
        var name: String? = null,
        @ManyToOne
        var library: Library? = null,
        @OneToMany(cascade = [ALL], mappedBy = "video", fetch = LAZY, orphanRemoval = true)
        var metadata: MutableList<MetadataValue<*>> = ArrayList(),
        @OneToMany(cascade = [ALL], mappedBy = "video", fetch = LAZY, orphanRemoval = true)
        var thumbnails: MutableList<Thumbnail> = ArrayList(),
        var selectedThumbnail: Int? = 0
) : BasePersistable<Int>() {
    @get:Transient
    val currentThumbnail: Thumbnail
        get() {
            var thumbnailIndex = this.selectedThumbnail ?: 0
            thumbnailIndex = thumbnailIndex.coerceIn(this.thumbnails.indices)
            return this.thumbnails[thumbnailIndex]
        }

    fun addMetadataValue(value: MetadataValue<*>) {
        value.video = this
        metadata.add(value)
    }

    fun hasMetadata(metadataToCheck: Metadata) = metadata.any { existing ->
        existing.metadata?.id == metadataToCheck.id
    }
}

@Entity
data class Thumbnail(
        @Id @GeneratedValue
        var id: Long? = null,
        @field:Column(updatable = false)
        var location: String? = null,
        @ManyToOne
        @JsonIgnore
        var video: Video? = null
) {
    @get:Transient
    @get:JsonIgnore
    val locationPath: Path
        get() = Paths.get(location)
}
