package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    override fun hashCode(): Int {
        return value.hashCode()
    }

    abstract fun clone(): MetadataValue<T>
}

data class StringMetadataValue(override var value: String? = null) : MetadataValue<String>() {
    override fun clone(): MetadataValue<String> {
        return StringMetadataValue(value)
    }
}

data class NumberMetadataValue(override var value: Int? = null) : MetadataValue<Int>() {
    override fun clone(): MetadataValue<Int> {
        return NumberMetadataValue(value)
    }
}

data class TimestampMetadataValue(override var value: LocalDateTime? = LocalDateTime.now()) : MetadataValue<LocalDateTime>() {
    override fun clone(): MetadataValue<LocalDateTime> {
        return TimestampMetadataValue(value)
    }
}

data class TaglistMetadataValue(override var value: Set<String>? = emptySet()) : MetadataValue<Set<String>>() {
    override fun clone(): MetadataValue<Set<String>> {
        return TaglistMetadataValue(value)
    }
}

data class DateMetadataValue(override var value: LocalDate? = LocalDate.now()) : MetadataValue<LocalDate>() {
    override fun clone(): MetadataValue<LocalDate> {
        return DateMetadataValue(value)
    }
}

data class DurationMetadataValue(override var value: Duration? = Duration.ZERO) : MetadataValue<Duration>() {
    override fun clone(): MetadataValue<Duration> {
        return DurationMetadataValue(value)
    }
}

data class BooleanMetadataValue(override var value: Boolean? = false) : MetadataValue<Boolean>() {
    override fun clone(): MetadataValue<Boolean> {
        return BooleanMetadataValue(value)
    }
}

data class SelectionMetadataValue(override var value: Int? = null) : MetadataValue<Int>() {
    override fun clone(): MetadataValue<Int> {
        return SelectionMetadataValue(value)
    }
}

data class FloatMetadataValue(override var value: Double? = null) : MetadataValue<Double>() {
    override fun clone(): MetadataValue<Double> {
        return FloatMetadataValue(value)
    }
}

data class TimeMetadataValue(override var value: LocalTime? = null) : MetadataValue<LocalTime>() {
    override fun clone(): MetadataValue<LocalTime> {
        return TimeMetadataValue(value)
    }
}

