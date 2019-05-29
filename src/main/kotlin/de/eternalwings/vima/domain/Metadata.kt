package de.eternalwings.vima.domain

import de.eternalwings.vima.MetadataType
import de.eternalwings.vima.domain.Ordering.ASC
import javax.persistence.CascadeType.ALL
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.FetchType.EAGER
import javax.persistence.OneToOne
import javax.validation.constraints.NotBlank

@Entity
data class Metadata(
        @NotBlank
        var name: String? = null,
        @NotBlank
        var type: MetadataType? = null,
        @Enumerated(STRING)
        var ordering: Ordering = ASC,
        var readOnly: Boolean = false,
        @Column(updatable = false)
        var systemSpecified: Boolean = false,
        @OneToOne(fetch = EAGER, cascade = [ALL], orphanRemoval = true)
        var options: MetadataOptions? = null,
        var displayOrder: Int = 0
): BasePersistable<Int>() {
    fun toValue() : MetadataValue<*> {
        val value = options!!.toValue()
        value.metadata = this
        return value
    }
}
