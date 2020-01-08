package de.eternalwings.vima.sqlite

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import de.eternalwings.vima.domain.MetadataValue
import org.apache.commons.lang3.StringUtils
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class SQLiteMetadataValueJsonConverter : AttributeConverter<Map<Int,MetadataValue<*>>, String> {
    private val writer: ObjectWriter
    private val reader: ObjectReader

    init {
        val mapper = ObjectMapper()
        mapper.findAndRegisterModules()
        val type = object : TypeReference<Map<Int, MetadataValue<*>>>() {}
        writer = mapper.writerFor(type)
        reader = mapper.readerFor(type)
    }

    override fun convertToDatabaseColumn(attribute: Map<Int, MetadataValue<*>>?): String {
        if (attribute == null) return ""
        return writer.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Map<Int, MetadataValue<*>> {
        if (StringUtils.isEmpty(dbData)) {
            throw IllegalStateException()
        }
        return reader.readValue(dbData!!)
    }
}
