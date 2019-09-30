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
        @OneToMany(cascade = [ALL], mappedBy = "video")
        var metadata: MutableList<MetadataValueContainer>? = mutableListOf(),
        @OneToMany(cascade = [ALL], mappedBy = "video", fetch = LAZY, orphanRemoval = true)
        var thumbnails: MutableList<Thumbnail> = mutableListOf(),
        var selectedThumbnail: Int? = 0
) : BasePersistable<Int>() {

    fun addMetadataValue(value: MetadataValueContainer) {
        metadata?.add(value)
        value.video = this
    }

    fun hasMetadata(metadataToCheck: Metadata) = metadata?.any { existing ->
        existing.definition?.id == metadataToCheck.id
    } ?: false
}

@Entity
data class Thumbnail(
        @Id @GeneratedValue
        var id: Int? = null,
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
