package de.eternalwings.vima.query

import de.eternalwings.vima.MetadataType.BOOLEAN
import de.eternalwings.vima.MetadataType.FLOAT
import de.eternalwings.vima.MetadataType.NUMBER
import de.eternalwings.vima.MetadataType.RANGE
import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.MetadataType.TAGLIST
import de.eternalwings.vima.MetadataType.TEXT
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.query.Comparator.EQUALS
import de.eternalwings.vima.query.Comparator.GREATER
import de.eternalwings.vima.query.Comparator.GREATER_OR_EQUALS
import de.eternalwings.vima.query.Comparator.SMALLER
import de.eternalwings.vima.query.Comparator.SMALLER_OR_EQUALS
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.stereotype.Component

@Component
class DatabaseQueryCreator(private val metadataRepository: MetadataRepository) {

    class MetadataNotValidException(metadataName: String) : RuntimeException("Metadata with name $metadataName does not exist.")
    class InvalidQueryOperationException(val type: QueryPart) : RuntimeException()

    @Throws(MetadataNotValidException::class)
    fun createQueryFrom(query: FullQuery): String {
        val allMetadata = this.metadataRepository.findAll()
        return createQueryFrom(query.part, TABLE_NAME, allMetadata)
    }

    private fun createQueryFrom(queryPart: QueryPart, model: String, metadataList: List<Metadata>): String {
        val modelId = "v"
        val start = "SELECT $modelId.id FROM $model $modelId WHERE "
        return start + createQueryCondition(queryPart, metadataList, modelId)
    }

    private fun createQueryCondition(queryPart: QueryPart, metadataList: List<Metadata>, modelId: String): String {
        return when (queryPart) {
            is UnionQuery -> "(" + queryPart.parts.joinToString(separator = " or ") {
                createQueryCondition(it, metadataList, modelId)
            } + ")"
            is IntersectionQuery -> "(" + queryPart.parts.joinToString(separator = " and ") {
                createQueryCondition(it, metadataList, modelId)
            } + ")"
            is PropertyQuery -> createPropertyQuery(queryPart, metadataList, modelId)
            is BooleanQuery -> createBooleanQuery(queryPart, metadataList, modelId)
            is TextQuery -> createTextQuery(queryPart, modelId)
            is ComparisonQuery -> createComparisonQuery(queryPart, metadataList, modelId)
            else -> throw InvalidQueryOperationException(queryPart)
        }
    }

    private fun createBooleanQuery(booleanQuery: BooleanQuery, metadataList: List<Metadata>, model: String): String {
        when (val inner = booleanQuery.query) {
            is TextQuery -> {
                val metadata = metadataList.getByName(inner.text) ?: throw MetadataNotValidException(inner.text)
                if (metadata.type != BOOLEAN) throw InvalidQueryOperationException(inner)
                val booleanValue = booleanQuery.value
                return metadataValueQuery(model, and(
                        metadataIdQuery(VIDEO_METADATA_MODEL_NAME, metadata),
                        booleanQueryOrDefault(booleanValue)
                ))
            }
            is PropertyQuery -> {
                val metadata = metadataList.getByName(inner.property) ?: throw MetadataNotValidException(inner.property)
                if (metadata.type != TAGLIST) throw InvalidQueryOperationException(inner)
                return metadataValueQuery(model, and(
                        metadataIdQuery(VIDEO_METADATA_MODEL_NAME, metadata),
                        arrayContainsQueryOrDefault(inner.value, booleanQuery.value)
                ))
            }
            is ComparisonQuery -> {
                val metadata = metadataList.getByName(inner.property) ?: throw MetadataNotValidException(inner.property)
                if (metadata.type != TAGLIST) throw InvalidQueryOperationException(inner)
                val comparator = if (booleanQuery.value) inner.comparator else inner.comparator.inverse()
                return metadataValueQuery(model, and(
                        metadataIdQuery(VIDEO_METADATA_MODEL_NAME, metadata),
                        arraySizeQueryOrDefault(comparator, inner.value.toInt())
                ))
            }
            else -> throw InvalidQueryOperationException(inner)
        }
    }

    private fun arraySizeQueryOrDefault(comparator: Comparator, size: Int): String {
        return or(
                jsonArraySize(VIDEO_METADATA_MODEL_NAME, comparator, size),
                and(
                        jsonArraySize(VIDEO_METADATA_MODEL_NAME, EQUALS, 0),
                        jsonArraySize(METADATA_MODEL_NAME, comparator, size, property = "defaultValue", column = "options")
                ).toString()
        ).toString()
    }

    private fun arrayContainsQueryOrDefault(value: String, contained: Boolean = true): String {
        return or(
                tagValueContainsQuery(VIDEO_METADATA_MODEL_NAME, value, contained),
                and(
                        jsonNullMatch(VIDEO_METADATA_MODEL_NAME),
                        tagValueContainsQuery(METADATA_MODEL_NAME, value, contained, property = "defaultValue",
                                column = "options")
                ).toString()
        ).toString()
    }

    private fun booleanQueryOrDefault(booleanValue: Boolean): String {
        return or(
                booleanValueQuery(VIDEO_METADATA_MODEL_NAME, booleanValue),
                and(
                        jsonNullMatch(VIDEO_METADATA_MODEL_NAME),
                        jsonValueMatch(METADATA_MODEL_NAME, booleanValue, property = "defaultValue",
                                column = "options")
                ).toString()
        ).toString()
    }

    private fun valueQueryOrDefault(value: Any?, comparator: Comparator = EQUALS): String {
        return or(
                jsonValueMatch(VIDEO_METADATA_MODEL_NAME, value, operator = comparator.toDB()),
                and(
                        jsonNullMatch(VIDEO_METADATA_MODEL_NAME),
                        jsonValueMatch(METADATA_MODEL_NAME, value, property = "defaultValue", column = "options",
                                operator = comparator.toDB())
                ).toString()
        ).toString()
    }

    private fun textQueryOrDefault(value: String, exact: Boolean = false): String {
        return or(
                textValueQuery(VIDEO_METADATA_MODEL_NAME, value, exact),
                and(
                        jsonNullMatch(VIDEO_METADATA_MODEL_NAME),
                        textValueQuery(METADATA_MODEL_NAME, value, exact, property = "defaultValue", column = "options")
                ).toString()
        ).toString()
    }

    private fun createTextQuery(textQuery: TextQuery, modelId: String): String {
        return metadataValueQuery(modelId, and(textQueryOrDefault(textQuery.text)))
    }

    private fun createPropertyQuery(propertyQuery: PropertyQuery, metadataList: List<Metadata>,
                                    modelId: String, inverse: Boolean = false): String {
        val foundMetadata =
                metadataList.getByName(propertyQuery.property) ?: throw MetadataNotValidException(propertyQuery.property)

        val valueQuery = when (foundMetadata.type) {
            BOOLEAN -> booleanQueryOrDefault(propertyQuery.value.toBoolean())
            TAGLIST -> arrayContainsQueryOrDefault(propertyQuery.value)
            TEXT -> textQueryOrDefault(propertyQuery.value, true)
            FLOAT -> valueQueryOrDefault(propertyQuery.value.toFloat())
            NUMBER, RANGE -> valueQueryOrDefault(propertyQuery.value.toInt())
            SELECTION -> valueQueryOrDefault(propertyQuery.value)
            else -> throw NotImplementedError()
        }

        return metadataValueQuery(modelId, and(
                metadataIdQuery(VIDEO_METADATA_MODEL_NAME, foundMetadata),
                valueQuery
        ), inverse)
    }

    private fun createComparisonQuery(comparisonQuery: ComparisonQuery, metadataList: List<Metadata>,
                                      modelId: String, inverse: Boolean = false): String {
        val foundMetadata =
                metadataList.getByName(comparisonQuery.property) ?: throw MetadataNotValidException(comparisonQuery.property)

        val comparator = if (inverse) comparisonQuery.comparator.inverse() else comparisonQuery.comparator
        val valueQuery = when (foundMetadata.type) {
            FLOAT -> valueQueryOrDefault(comparisonQuery.value.toDouble(), comparator)
            NUMBER, RANGE -> valueQueryOrDefault(comparisonQuery.value.toInt(), comparator)
            TAGLIST -> arraySizeQueryOrDefault(comparator, comparisonQuery.value.toInt())
            else -> throw NotImplementedError()
        }
        return metadataValueQuery(modelId, and(
                metadataIdQuery(VIDEO_METADATA_MODEL_NAME, foundMetadata),
                valueQuery
        ))
    }

    private fun Comparator.toDB(): String {
        return when (this) {
            GREATER -> ">"
            SMALLER -> "<"
            GREATER_OR_EQUALS -> ">="
            SMALLER_OR_EQUALS -> "<="
            EQUALS -> "="
        }
    }

    private fun metadataValueQuery(model: String, parts: Filter, inverse: Boolean = false): String {
        val queryType = if (inverse) "NOT EXISTS" else "EXISTS"
        return "$queryType(SELECT * FROM video_metadata $VIDEO_METADATA_MODEL_NAME LEFT JOIN $METADATA_TABLE_NAME " +
                "$METADATA_MODEL_NAME ON $VIDEO_METADATA_MODEL_NAME.definition_id = $METADATA_MODEL_NAME.id WHERE " +
                "$VIDEO_METADATA_MODEL_NAME.video_id = $model.id AND $parts)"
    }

    private fun metadataIdQuery(table: String, metadata: Metadata): String {
        return "$table.definition_id = ${metadata.id}"
    }

    private fun tagValueContainsQuery(table: String, value: String, isContained: Boolean = true, property: String = "value",
                                      column: String = "value") =
            jsonArrayContains(table, property, "'$value'", isContained, column = column)

    private fun textValueQuery(table: String, value: String, exact: Boolean = false, property: String = "value",
                               column: String = "value"): String {
        return when (exact) {
            false -> jsonValueMatch(table, "'%$value%'", property, "LIKE", column = column)
            true -> jsonValueMatch(table, "'$value'", property, column = column)
        }
    }

    private fun booleanValueQuery(table: String, value: Boolean, property: String = "value") =
            jsonValueMatch(table, if (value) 1 else 0, property)

    private fun jsonNullMatch(table: String, property: String = "value", column: String = "value") =
            jsonValueMatch(table, null, property, "IS", column)

    private fun jsonValueMatch(table: String, value: Any?, property: String = "value", operator: String = "=",
                               column: String = "value"): String {
        return "json_extract($table.$column, '$.$property') $operator $value"
    }

    private fun jsonArrayContains(table: String, property: String, value: String, isContained: Boolean = true,
                                  operator: String = "=", column: String = "value"): String {
        val queryType = if (!isContained) "NOT EXISTS" else "EXISTS"
        val iterator = "json_each(json_extract($table.$column, '$.$property'))"
        return "$queryType(SELECT * FROM $iterator WHERE json_each.value $operator $value)"
    }

    private fun jsonArraySize(table: String, comparator: Comparator, value: Number, property: String = "value",
                              column: String = "value"): String {
        return "json_array_length($table.$column, '$.$property') ${comparator.toDB()} $value"
    }

    private fun List<Metadata>.getByName(name: String): Metadata? {
        return this.find { it.name == name }
    }

    companion object {
        private const val TABLE_NAME = "Video"
        private const val METADATA_TABLE_NAME = "Metadata"
        private const val VIDEO_METADATA_MODEL_NAME = "vm"
        private const val METADATA_MODEL_NAME = "m"
    }
}
