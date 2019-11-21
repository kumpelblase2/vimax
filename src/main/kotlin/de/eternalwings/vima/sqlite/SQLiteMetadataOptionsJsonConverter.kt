package de.eternalwings.vima.sqlite

import com.fasterxml.jackson.databind.ObjectMapper
import de.eternalwings.vima.domain.MetadataOptions
import org.apache.commons.lang3.StringUtils
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class SQLiteMetadataOptionsJsonConverter : AttributeConverter<MetadataOptions<*>, String> {
    private val objectMapper = ObjectMapper()

    init {
        objectMapper.findAndRegisterModules()
    }

    override fun convertToDatabaseColumn(attribute: MetadataOptions<*>?): String {
        if (attribute == null) return ""
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): MetadataOptions<*>? {
        if (StringUtils.isEmpty(dbData)) {
            return null
        }
        return objectMapper.readValue(dbData!!, MetadataOptions::class.java)
    }
}
