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
@JsonSubTypes(value = [
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
])
abstract class MetadataOptions<T>(val type: MetadataType, @get:JsonIgnore val metadataConstructor: (T?) -> MetadataValue<T>,
                                  var defaultValue: T? = null) {
    @JsonIgnore
    fun toValue(): MetadataValue<T> = metadataConstructor(defaultValue)
}

class TextMetadataOptions(val suggest: Boolean = false) : MetadataOptions<String>(TEXT, ::StringMetadataValue)

class NumberMetadataOptions(val min: Int? = null, val max: Int? = null, val step: Int? = null) :
        MetadataOptions<Int>(NUMBER, ::NumberMetadataValue)

class RangeMetadataOptions(val min: Int? = null, val max: Int? = null, val step: Int? = null) :
        MetadataOptions<Int>(RANGE, ::NumberMetadataValue)

class DurationMetadataOptions(val min: Int? = null, val max: Int? = null, val step: Int? = null) :
        MetadataOptions<Duration>(DURATION, ::DurationMetadataValue)

class SelectionMetadataOptions(val values: List<SelectionValues> = emptyList()) :
        MetadataOptions<SelectionValues>(SELECTION, ::SelectionMetadataValue)

class SelectionValues {
    var name: String? = null

    constructor() {}

    constructor(name: String) {
        this.name = name
    }
}

class TaglistMetadataOptions : MetadataOptions<List<String>>(TAGLIST, ::TaglistMetadataValue)

class BooleanMetadataOptions : MetadataOptions<Boolean>(BOOLEAN, ::BooleanMetadataValue)

class TimeMetadataOptions : MetadataOptions<LocalTime>(TIME, ::TimeMetadataValue)

class DateTimeMetadataOptions : MetadataOptions<LocalDateTime>(DATETIME, ::TimestampMetadataValue)

class DateMetadataOptions : MetadataOptions<LocalDate>(DATE, ::DateMetadataValue)

class FloatMetadataOptions : MetadataOptions<Double>(FLOAT, ::FloatMetadataValue)
