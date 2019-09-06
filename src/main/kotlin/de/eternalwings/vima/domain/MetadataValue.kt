package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import de.eternalwings.vima.ext.PropertyDelegate
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.OneToOne
import javax.persistence.Transient

@JsonTypeInfo(use = NAME, include = PROPERTY, property = "meta-type", visible = false)
@JsonSubTypes(value = [
    JsonSubTypes.Type(name = "TEXT", value = StringMetadataValue::class),
    JsonSubTypes.Type(name = "NUMBER", value = NumberMetadataValue::class),
    //    JsonSubTypes.Type(name = "RANGE", value = RMV::class),
    JsonSubTypes.Type(name = "DURATION", value = DurationMetadataValue::class),
    JsonSubTypes.Type(name = "SELECTION", value = SelectionMetadataValue::class),
    JsonSubTypes.Type(name = "TAGLIST", value = TaglistMetadataValue::class),
    JsonSubTypes.Type(name = "BOOLEAN", value = BooleanMetadataValue::class),
    JsonSubTypes.Type(name = "DATE", value = DateMetadataValue::class),
    JsonSubTypes.Type(name = "DATETIME", value = TimestampMetadataValue::class),
    JsonSubTypes.Type(name = "TIME", value = TimeMetadataValue::class),
    JsonSubTypes.Type(name = "FLOAT", value = FloatMetadataValue::class)
])
sealed class MetadataValue<T> {
    @get:Transient
    abstract var value: T?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetadataValue<*>) return false
        if (other.value != this.value) return false

        return true
    }

    fun copyFrom(other: MetadataValue<T>) {
        this.value = other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

data class StringMetadataValue(override var value: String? = null) : MetadataValue<String>() {
}

data class NumberMetadataValue(override var value: Int? = null) : MetadataValue<Int>() {
}

data class TimestampMetadataValue(override var value: LocalDateTime? = LocalDateTime.now()) : MetadataValue<LocalDateTime>() {
}

data class TaglistMetadataValue(override var value: List<String>? = emptyList()) : MetadataValue<List<String>>() {
}

data class DateMetadataValue(override var value: LocalDate? = LocalDate.now()) : MetadataValue<LocalDate>() {
}

data class DurationMetadataValue(override var value: Duration? = Duration.ZERO) : MetadataValue<Duration>() {
}

data class BooleanMetadataValue(override var value: Boolean? = false) : MetadataValue<Boolean>() {
}

data class SelectionMetadataValue(override var value: SelectionValues? = null) : MetadataValue<SelectionValues>() {
}

data class FloatMetadataValue(override var value: Double? = null) : MetadataValue<Double>() {
}

data class TimeMetadataValue(override var value: LocalTime? = null) : MetadataValue<LocalTime>() {
}
