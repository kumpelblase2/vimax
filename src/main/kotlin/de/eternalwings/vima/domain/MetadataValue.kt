package de.eternalwings.vima.domain

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.DiscriminatorColumn
import javax.persistence.DiscriminatorType.INTEGER
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType.JOINED
import javax.persistence.ManyToOne

@Entity // Since we reference this, we cannot make it a @MappedSuperclass
@Inheritance(strategy = JOINED)
@DiscriminatorColumn(discriminatorType = INTEGER)
sealed class MetadataValue<T>(
        @Id @GeneratedValue
        var id: Int? = null,
        var name: String? = null,
        @ManyToOne
        var metadata: Metadata? = null
) {
    abstract val value: T?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetadataValue<*>) return false

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}

@Entity
@DiscriminatorValue("1")
data class StringMetadataValue(var stringValue: String? = null) : MetadataValue<String>() {
    override val value: String?
        get() = stringValue
}

@Entity
@DiscriminatorValue("2")
data class NumberMetadataValue(var numberValue: Int? = null) : MetadataValue<Int>() {
    override val value: Int?
        get() = numberValue
}

@Entity
@DiscriminatorValue("4")
data class TimestampMetadataValue(var timestampValue: LocalDateTime = LocalDateTime.now()) :
        MetadataValue<LocalDateTime>() {
    override val value: LocalDateTime?
        get() = timestampValue
}
