package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import de.eternalwings.vima.sqlite.SQLiteMetadataValueJsonConverter
import java.nio.file.Path
import java.nio.file.Paths
import javax.persistence.CascadeType.ALL
import javax.persistence.Column
import javax.persistence.Convert
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
        @field:ManyToOne(optional = false)
        var library: Library? = null,
        @Suppress("JpaAttributeTypeInspection")
        @field:Column(columnDefinition = "text", name = "metadata_values")
        @field:Convert(converter = SQLiteMetadataValueJsonConverter::class)
        var metadata: MutableMap<Int, MetadataValue<*>>? = null,
        @field:OneToMany(cascade = [ALL], mappedBy = "video", fetch = LAZY, orphanRemoval = true)
        var thumbnails: MutableList<Thumbnail> = mutableListOf(),
        var selectedThumbnail: Int? = 0
) : BasePersistable<Int>() {

    fun addMetadataValue(metadataId: Int, value: MetadataValue<*>) {
        metadata?.put(metadataId, value)
    }

    fun hasMetadata(metadataToCheck: Metadata) = hasMetadata(metadataToCheck.id!!)

    fun hasMetadata(metadataId: Int) = metadata?.containsKey(metadataId) ?: false
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
