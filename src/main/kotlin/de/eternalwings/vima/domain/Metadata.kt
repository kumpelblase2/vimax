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
import javax.validation.constraints.NotBlank

@Entity
data class Metadata(
        @NotBlank
        var name: String? = null,
        @NotBlank
        var type: MetadataType? = null,
        @Enumerated(STRING)
        var ordering: Direction = ASC,
        var readOnly: Boolean = false,
        @Column(updatable = false)
        var systemSpecified: Boolean = false,
        @Column(columnDefinition = "text")
        @Convert(converter = SQLiteMetadataOptionsJsonConverter::class)
        var options: MetadataOptions<*>? = null,
        var displayOrder: Int = 0
) : BasePersistable<Int>() {
    fun toValue(): MetadataValueContainer {
        return MetadataValueContainer(null, this, options!!.toValue())
    }
}
