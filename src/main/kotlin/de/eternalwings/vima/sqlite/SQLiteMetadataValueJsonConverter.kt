package de.eternalwings.vima.sqlite

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.eternalwings.vima.domain.MetadataValue
import org.apache.commons.lang3.StringUtils
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class SQLiteMetadataValueJsonConverter : AttributeConverter<MetadataValue<*>, String> {
    private val objectMapper = ObjectMapper()
    private val type: TypeReference<MetadataValue<*>> = object : TypeReference<MetadataValue<*>>() {}

    override fun convertToDatabaseColumn(attribute: MetadataValue<*>?): String {
        if (attribute == null) return ""
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): MetadataValue<*> {
        if (StringUtils.isEmpty(dbData)) {
            throw IllegalStateException()
        }
        return objectMapper.readValue(dbData!!, type)
    }
}
