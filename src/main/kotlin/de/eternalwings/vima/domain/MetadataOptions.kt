package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import de.eternalwings.vima.MetadataType
import de.eternalwings.vima.MetadataType.BOOLEAN
import de.eternalwings.vima.MetadataType.DATE
import de.eternalwings.vima.MetadataType.DATETIME
import de.eternalwings.vima.MetadataType.DURATION
import de.eternalwings.vima.MetadataType.FLOAT
import de.eternalwings.vima.MetadataType.NUMBER
import de.eternalwings.vima.MetadataType.RANGE
import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.MetadataType.TAGLIST
import de.eternalwings.vima.MetadataType.TEXT
import de.eternalwings.vima.MetadataType.TIME
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@JsonTypeInfo(use = NAME, include = EXISTING_PROPERTY, property = "type", visible = false)
@JsonSubTypes(
    value = [
        Type(name = "TEXT", value = TextMetadataOptions::class),
        Type(name = "NUMBER", value = NumberMetadataOptions::class),
        Type(name = "RANGE", value = RangeMetadataOptions::class),
        Type(name = "DURATION", value = DurationMetadataOptions::class),
        Type(name = "SELECTION", value = SelectionMetadataOptions::class),
        Type(name = "TAGLIST", value = TaglistMetadataOptions::class),
        Type(name = "BOOLEAN", value = BooleanMetadataOptions::class),
        Type(name = "DATE", value = DateMetadataOptions::class),
        Type(name = "DATETIME", value = DateTimeMetadataOptions::class),
        Type(name = "TIME", value = TimeMetadataOptions::class),
        Type(name = "FLOAT", value = FloatMetadataOptions::class)
    ]
)
abstract class MetadataOptions<T, S>(
    val type: MetadataType, @get:JsonIgnore val metadataConstructor: (T?) -> MetadataValue<S>,
    var defaultValue: T? = null
) {
    @JsonIgnore
    fun toValue(): MetadataValue<S> = metadataConstructor(null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetadataOptions<*, *>) return false

        if (type != other.type) return false
        if (defaultValue != other.defaultValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (defaultValue?.hashCode() ?: 0)
        return result
    }

}

abstract class SimpleMetadataOptions<T>(
    type: MetadataType,
    metadataConstructor: (T?) -> MetadataValue<T>,
    defaultValue: T? = null
) : MetadataOptions<T, T>(type, metadataConstructor, defaultValue)

data class TextMetadataOptions(val suggest: Boolean = false) : SimpleMetadataOptions<String>(TEXT, ::StringMetadataValue)

data class NumberMetadataOptions(val min: Int? = null, val max: Int? = null, val step: Int? = null) :
    SimpleMetadataOptions<Int>(NUMBER, ::NumberMetadataValue)

data class RangeMetadataOptions(val min: Int? = null, val max: Int? = null, val step: Int? = null) :
    SimpleMetadataOptions<Int>(RANGE, ::NumberMetadataValue)

data class DurationMetadataOptions(val min: Int? = null, val max: Int? = null, val step: Int? = null) :
    SimpleMetadataOptions<Duration>(DURATION, ::DurationMetadataValue)

data class SelectionMetadataOptions(val values: List<SelectionValue> = emptyList()) :
    MetadataOptions<SelectionValue, Int>(SELECTION, { selection -> SelectionMetadataValue(selection?.id) })

data class SelectionValue(var id: Int? = null, var name: String? = null)

class TaglistMetadataOptions : SimpleMetadataOptions<Set<String>>(TAGLIST, ::TaglistMetadataValue)

class BooleanMetadataOptions : SimpleMetadataOptions<Boolean>(BOOLEAN, ::BooleanMetadataValue)

class TimeMetadataOptions : SimpleMetadataOptions<LocalTime>(TIME, ::TimeMetadataValue)

class DateTimeMetadataOptions : SimpleMetadataOptions<LocalDateTime>(DATETIME, ::TimestampMetadataValue)

class DateMetadataOptions : SimpleMetadataOptions<LocalDate>(DATE, ::DateMetadataValue)

class FloatMetadataOptions : SimpleMetadataOptions<Double>(FLOAT, ::FloatMetadataValue)
