package de.eternalwings.vima.domain

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.FetchType.LAZY
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
@Immutable
data class FullMetadataValue(
        @Id
        var id: Int? = null,
        @ManyToOne(fetch = LAZY)
        var metadata: Metadata? = null,
        @ManyToOne(fetch = LAZY)
        var video: Video? = null,
        var booleanValue: Boolean? = null,
        var dateValue: LocalDate? = null,
        var floatingValue: Double? = null,
        var numberValue: Long? = null,
        @ManyToOne(fetch = EAGER)
        var selectionValue: SelectionValues? = null,
        var stringValue: String? = null,
        @Type(type = "varchar-array")
        var taglistValues: Array<String>? = null,
        var timeValue: LocalTime? = null,
        var timestampValue: LocalDateTime? = null
)
