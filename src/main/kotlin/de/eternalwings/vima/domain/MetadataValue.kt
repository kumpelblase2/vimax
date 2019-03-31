package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import de.eternalwings.vima.ext.PropertyDelegate
import org.hibernate.annotations.Type
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.DiscriminatorColumn
import javax.persistence.DiscriminatorType.INTEGER
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Transient

@Entity // Since we reference this, we cannot make it a @MappedSuperclass
@DiscriminatorColumn(discriminatorType = INTEGER)
@JsonTypeInfo(use = NAME, include = PROPERTY, property = "meta-type")
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
sealed class MetadataValue<T>(
        @Id @GeneratedValue
        var id: Int? = null,
        @ManyToOne
        open var metadata: Metadata? = null,
        @ManyToOne
        @JsonIgnore
        open var video: Video? = null
) {
    @get:Transient
    abstract var value: T?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetadataValue<*>) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (video?.hashCode() ?: 0)
        return result
    }

    fun copyFrom(other: MetadataValue<T>) {
        this.value = other.value
    }
}

@Entity
@DiscriminatorValue("1")
data class StringMetadataValue(var stringValue: String? = null) : MetadataValue<String>() {
    @get:Transient
    @delegate:Transient
    override var value: String? by PropertyDelegate(this::stringValue)
}

@Entity
@DiscriminatorValue("2")
data class NumberMetadataValue(var numberValue: Int? = null) : MetadataValue<Int>() {
    @get:Transient
    @delegate:Transient
    override var value: Int? by PropertyDelegate(this::numberValue)
}

@Entity
@DiscriminatorValue("4")
data class TimestampMetadataValue(var timestampValue: LocalDateTime? = LocalDateTime
    .now()) :
        MetadataValue<LocalDateTime>() {
    @get:Transient
    @delegate:Transient
    override var value: LocalDateTime? by PropertyDelegate(this::timestampValue)
}

@Entity
@DiscriminatorValue("5")
data class TaglistMetadataValue(@Type(type = "de.eternalwings.vima.hsql.StringArrayType")
                                var taglistValues: Array<String> = emptyArray()) :
        MetadataValue<Array<String>>() {
    @get:Transient
    @delegate:Transient
    override var value: Array<String>? by PropertyDelegate(this::taglistValues)
}

@Entity
@DiscriminatorValue("6")
data class DateMetadataValue(var dateValue: LocalDate? = LocalDate.now()) :
        MetadataValue<LocalDate>() {
    @get:Transient
    @delegate:Transient
    override var value: LocalDate? by PropertyDelegate(this::dateValue)
}

@Entity
@DiscriminatorValue("7")
data class DurationMetadataValue(var durationValue: Long? = 0) :
        MetadataValue<Duration>() {
    @get:Transient
    override var value: Duration?
        get() = Duration.ofMillis(this.durationValue ?: 0)
        set(value) {
            this.durationValue = value?.toMillis() ?: 0
        }
}

@Entity
@DiscriminatorValue("8")
data class BooleanMetadataValue(var booleanValue: Boolean? = false) :
        MetadataValue<Boolean>() {
    @get:Transient
    @delegate:Transient
    override var value: Boolean? by PropertyDelegate(this::booleanValue)
}

@Entity
@DiscriminatorValue("9")
data class SelectionMetadataValue(@OneToOne var selectionValue: SelectionValues? = null) :
        MetadataValue<SelectionValues>() {
    @get:Transient
    @delegate:Transient
    override var value: SelectionValues? by PropertyDelegate(this::selectionValue)
}

@Entity
@DiscriminatorValue("10")
data class FloatMetadataValue(var floatingValue: Double? = null) : MetadataValue<Double>() {
    @get:Transient
    @delegate:Transient
    override var value: Double? by PropertyDelegate(this::floatingValue)
}

@Entity
@DiscriminatorValue("11")
data class TimeMetadataValue(var timeValue: LocalTime? = null) : MetadataValue<LocalTime>() {
    @get:Transient
    @delegate:Transient
    override var value: LocalTime? by PropertyDelegate(this::timeValue)
}
