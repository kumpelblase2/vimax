package de.eternalwings.vima.domain

import de.eternalwings.vima.sqlite.SQLiteMetadataValueJsonConverter
import org.springframework.data.domain.Persistable
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.FetchType.LAZY
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "video_metadata")
data class MetadataValueContainer(
        @Id @GeneratedValue(strategy = IDENTITY)
        private var id: Int? = null,
        @ManyToOne(optional = false)
        var definition: Metadata? = null,
        @Column(columnDefinition = "text")
        @Convert(converter = SQLiteMetadataValueJsonConverter::class)
        var value: MetadataValue<*>? = null,
        @ManyToOne(optional = false, fetch = LAZY)
        var video: Video? = null
) : Persistable<Int> {
    override fun getId() = id

    fun setId(id: Int?) {
        this.id = id
    }

    override fun isNew() = id == null
}
