package de.eternalwings.vima.domain

import de.eternalwings.vima.MetadataType
import de.eternalwings.vima.sqlite.SQLiteMetadataOptionsJsonConverter
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Direction.ASC
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.FetchType.LAZY
import javax.persistence.ManyToOne

@Entity
data class Metadata(
        @field:Column(updatable = false)
        var name: String? = null,
        @field:Column(updatable = false, nullable = false)
        var type: MetadataType? = null,
        @field:Enumerated(STRING)
        var ordering: Direction = ASC,
        var readOnly: Boolean = false,
        @field:ManyToOne(fetch = LAZY)
        var owner: PluginInformation? = null,
        @field:Column(columnDefinition = "text")
        @field:Convert(converter = SQLiteMetadataOptionsJsonConverter::class)
        var options: MetadataOptions<*,*>? = null,
        var displayOrder: Int = 0
) : BasePersistable<Int>() {
    fun toValue(): MetadataValue<*> {
        return options!!.toValue()
    }

    val isSystemSpecified: Boolean
        get() = owner != null
}
