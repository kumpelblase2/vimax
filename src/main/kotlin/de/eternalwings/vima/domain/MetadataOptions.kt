package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
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
import de.eternalwings.vima.ext.toLocalDate
import de.eternalwings.vima.ext.toLocalDateTime
import de.eternalwings.vima.ext.toLocalTime
import java.util.Date
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.TemporalType.TIMESTAMP
import javax.persistence.Transient

@Entity
@JsonTypeInfo(use = NAME, include = PROPERTY, property = "type")
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
abstract class MetadataOptions {
    @Id
    @GeneratedValue
    var id: Int? = null

    @Transient
    abstract fun getType(): MetadataType?

    @Transient
    abstract fun toValue(): MetadataValue<*>
}

@Entity
class TextMetadataOptions() : MetadataOptions() {
    constructor(id: Int, suggest: Boolean) : this() {
        this.id = id
        this.suggest = suggest
    }

    var suggest: Boolean = false

    var defaultTextValue: String? = null

    override fun getType() = TEXT

    override fun toValue() = StringMetadataValue(defaultTextValue)
}

@Entity
class NumberMetadataOptions() : MetadataOptions() {

    constructor(id: Int, min: Int, max: Int, step: Int) : this() {
        this.id = id
        this.min = min
        this.max = max
        this.step = step
    }

    var min: Int? = null
    var max: Int? = null
    var step: Int? = null
    var defaultNumberValue: Int? = null

    override fun getType() = NUMBER

    override fun toValue() = NumberMetadataValue(defaultNumberValue)
}

@Entity
class RangeMetadataOptions : MetadataOptions() {
    var min: Int? = null
    var max: Int? = null
    var step: Int? = null
    var defaultNumberValue: Int? = null

    override fun getType() = RANGE

    override fun toValue() = NumberMetadataValue(defaultNumberValue)
}

@Entity
class DurationMetadataOptions : MetadataOptions() {
    var min: Int? = null
    var max: Int? = null
    var step: Int? = null
    var defaultLongValue: Long? = null

    override fun getType() = DURATION

    override fun toValue() = DurationMetadataValue(defaultLongValue)
}

@Entity
class SelectionMetadataOptions : MetadataOptions() {

    @OneToMany(cascade = [ALL], orphanRemoval = true)
    var values: List<SelectionValues> = emptyList()
    @OneToOne
    var defaultSelectValue: SelectionValues? = null

    override fun getType() = SELECTION

    override fun toValue() = SelectionMetadataValue(defaultSelectValue)
}

@Entity
class SelectionValues {
    @Id
    @GeneratedValue
    var id: Long? = null

    var name: String? = null
}

@Entity
class TaglistMetadataOptions : MetadataOptions() {

    @field:org.hibernate.annotations.Type(type = "varchar-array")
    var defaultTagValues: Array<String> = emptyArray()

    override fun getType() = TAGLIST

    override fun toValue() = TaglistMetadataValue(defaultTagValues)
}

@Entity
class BooleanMetadataOptions : MetadataOptions() {
    var defaultBooleanValue: Boolean? = false

    override fun getType() = BOOLEAN

    override fun toValue() = BooleanMetadataValue(defaultBooleanValue)
}

@Entity
class TimeMetadataOptions : MetadataOptions() {
    @Temporal(TemporalType.TIME)
    var defaultTimeValue: Date? = null

    override fun getType() = TIME

    override fun toValue() = TimeMetadataValue(defaultTimeValue?.toLocalTime())
}

@Entity
class DateTimeMetadataOptions : MetadataOptions() {
    @Temporal(TIMESTAMP)
    var defaultTimestampValue: Date? = null

    override fun getType() = DATETIME

    override fun toValue() = TimestampMetadataValue(defaultTimestampValue?.toLocalDateTime())
}

@Entity
class DateMetadataOptions : MetadataOptions() {
    @Temporal(TemporalType.DATE)
    var defaultDateValue: Date? = null

    override fun getType() = DATE

    override fun toValue() = DateMetadataValue(defaultDateValue?.toLocalDate())
}

@Entity
class FloatMetadataOptions : MetadataOptions() {
    var defaultDoubleValue: Double? = null

    override fun getType() = FLOAT

    override fun toValue() = FloatMetadataValue(defaultDoubleValue)
}
