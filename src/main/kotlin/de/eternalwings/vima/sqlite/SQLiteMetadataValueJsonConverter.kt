package de.eternalwings.vima.sqlite

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.eternalwings.vima.domain.MetadataValue
import org.apache.commons.lang3.StringUtils
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class SQLiteMetadataValueJsonConverter : AttributeConverter<Map<Int,MetadataValue<*>>, String> {
    private val objectMapper = ObjectMapper()
    private val type: TypeReference<Map<Int, MetadataValue<*>>> = object : TypeReference<Map<Int, MetadataValue<*>>>() {}

    init {
        objectMapper.findAndRegisterModules()
    }

    override fun convertToDatabaseColumn(attribute: Map<Int, MetadataValue<*>>?): String {
        if (attribute == null) return ""
        return objectMapper.writer().forType(type).writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Map<Int, MetadataValue<*>> {
        if (StringUtils.isEmpty(dbData)) {
            throw IllegalStateException()
        }
        return objectMapper.readValue(dbData!!, type)
    }
}
