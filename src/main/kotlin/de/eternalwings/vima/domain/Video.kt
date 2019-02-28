package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.nio.file.Path
import java.nio.file.Paths
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Transient

@Entity
data class Video(
        var location: String? = null,
        var name: String? = null,
        @OneToMany(cascade = [ALL])
        var metadata: MutableList<MetadataValue<*>> = ArrayList(),
        @OneToMany(cascade = [ALL])
        var thumbnails: MutableList<Thumbnail> = ArrayList(),
        var selectedThumbnail: Short? = 0
) : BasePersistable<Int>()

@Entity
data class Thumbnail(
        @Id @GeneratedValue
        var id: Long? = null,
        var location: String? = null
) {
    @get:Transient
    @get:JsonIgnore
    val locationPath: Path
        get() = Paths.get(location)
}
