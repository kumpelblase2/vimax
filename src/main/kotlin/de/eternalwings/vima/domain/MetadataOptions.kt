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
import de.eternalwings.vima.MetadataType.NUMBER
import de.eternalwings.vima.MetadataType.RANGE
import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.MetadataType.TAGLIST
import de.eternalwings.vima.MetadataType.TEXT
import de.eternalwings.vima.MetadataType.TIME
import java.util.Date
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
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
    Type(name = "TIME", value = TimeMetadataOptions::class)
])
abstract class MetadataOptions {
    @Id
    @GeneratedValue
    var id: Int? = null

    @Transient
    abstract fun getType(): MetadataType?
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
}

@Entity
class RangeMetadataOptions : MetadataOptions() {
    var min: Int? = null
    var max: Int? = null
    var step: Int? = null
    var defaultNumberValue: Int? = null

    override fun getType() = RANGE
}

@Entity
class DurationMetadataOptions : MetadataOptions() {
    var min: Int? = null
    var max: Int? = null
    var step: Int? = null
    var defaultNumberValue: Int? = null

    override fun getType() = DURATION
}

@Entity
class SelectionMetadataOptions : MetadataOptions() {

    @OneToMany(cascade = [ALL], orphanRemoval = true)
    var values: List<SelectionValues> = emptyList()
    var defaultTextValue: String? = null

    override fun getType() = SELECTION
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

    var defaultTextValue: String? = null
    override fun getType() = TAGLIST

}

@Entity
class BooleanMetadataOptions : MetadataOptions() {
    var defaultBooleanValue: Boolean? = null

    override fun getType() = BOOLEAN
}

@Entity
class TimeMetadataOptions : MetadataOptions() {
    @Temporal(TemporalType.TIME)
    var defaultTimeValue: Date? = null

    override fun getType() = TIME
}

@Entity
class DateTimeMetadataOptions : MetadataOptions() {
    @Temporal(TIMESTAMP)
    var defaultTimestampValue: Date? = null

    override fun getType() = DATETIME
}

@Entity
class DateMetadataOptions : MetadataOptions() {
    @Temporal(TemporalType.DATE)
    var defaultDateValue: Date? = null

    override fun getType() = DATE
}
