package de.eternalwings.vima.query

import de.eternalwings.vima.MetadataType.BOOLEAN
import de.eternalwings.vima.MetadataType.DURATION
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
import de.eternalwings.vima.query.Comparator.NOT_EQUALS
import de.eternalwings.vima.query.Comparator.SMALLER
import de.eternalwings.vima.query.Comparator.SMALLER_OR_EQUALS
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class DatabaseQueryCreator(private val metadataRepository: MetadataRepository) {

    class MetadataNotValidException(metadataName: String) : RuntimeException("Metadata with name $metadataName does not exist.")
    class InvalidQueryOperationException(val type: QueryPart) : RuntimeException()

    class QueryContext {
        private var variables: List<Any?> = emptyList()

        val parameterMap: Iterable<Pair<Int, Any?>>
            get() = variables.mapIndexed { i, value -> (i + 1) to value }

        fun newVar(value: Any?): Int {
            variables = variables + value
            return variables.lastIndex + 1
        }
    }

    @Throws(MetadataNotValidException::class)
    fun createQueryFrom(query: FullQuery): Pair<String, QueryContext> {
        val allMetadata = this.metadataRepository.findAll()
        return createQueryFrom(query.part, allMetadata)
    }

    private fun createQueryFrom(queryPart: QueryPart, metadataList: List<Metadata>): Pair<String, QueryContext> {
        val start = "SELECT $VIDEO_MODEL_NAME.id FROM $TABLE_NAME $VIDEO_MODEL_NAME WHERE "
        val context = QueryContext()
        return (start + createQueryCondition(queryPart, metadataList, context)) to context
    }

    private fun createQueryCondition(queryPart: QueryPart, metadataList: List<Metadata>, context: QueryContext): String {
        return when (queryPart) {
            is UnionQuery -> "(" + queryPart.parts.joinToString(separator = " or ") {
                createQueryCondition(it, metadataList, context)
            } + ")"
            is IntersectionQuery -> "(" + queryPart.parts.joinToString(separator = " and ") {
                createQueryCondition(it, metadataList, context)
            } + ")"
            is PropertyQuery -> createPropertyQuery(queryPart.property, queryPart.value, metadataList, context)
            is BooleanQuery -> createBooleanQuery(queryPart, metadataList, context)
            is TextQuery -> createTextQuery(queryPart.text, metadataList, context)
            is ComparisonQuery -> createComparisonQuery(queryPart.property, queryPart.comparator, queryPart.value, metadataList,
                    context)
            else -> throw InvalidQueryOperationException(queryPart)
        }
    }

    private fun createBooleanQuery(booleanQuery: BooleanQuery, metadataList: List<Metadata>, context: QueryContext): String {
        return when (val inner = booleanQuery.query) {
            is TextQuery -> {
                val propertyName = inner.text
                val property = metadataList.getByName(propertyName)
                if (property != null) {
                    when (property.type) {
                        TAGLIST -> createComparisonQuery(propertyName, EQUALS, "0", metadataList, context, booleanQuery.value)
                        else -> createPropertyQuery(propertyName, "", metadataList, context, booleanQuery.value)
                    }
                } else {
                    createTextQuery(propertyName, metadataList, context, !booleanQuery.value)
                }
            }
            is PropertyQuery -> createPropertyQuery(inner.property, inner.value, metadataList, context, !booleanQuery.value)
            is ComparisonQuery -> createComparisonQuery(inner.property, inner.comparator, inner.value, metadataList, context,
                    !booleanQuery.value)
            else -> throw InvalidQueryOperationException(inner)
        }
    }

    private fun createPropertyQuery(property: String, value: String, metadataList: List<Metadata>, context: QueryContext,
                                    inverse: Boolean = false): String {
        val foundMetadata =
                metadataList.getByName(property) ?: throw MetadataNotValidException(property)

        val comparator = if (inverse) EQUALS.inverse() else EQUALS
        return when (foundMetadata.type) {
            BOOLEAN -> {
                val booleanValue = value.toBoolean()
                booleanQueryOrDefault(foundMetadata.id!!, if (inverse) booleanValue.not() else booleanValue, context)
            }
            TAGLIST -> arrayContainsQueryOrDefault(foundMetadata.id!!, value, context, !inverse)
            TEXT -> textQueryOrDefault(foundMetadata.id!!, value, context, true, inverse)
            FLOAT -> valueQueryOrDefault(foundMetadata.id!!, value.toFloat(), context, comparator)
            NUMBER, RANGE -> valueQueryOrDefault(foundMetadata.id!!, value.toInt(), context, comparator)
            SELECTION -> valueQueryOrDefault(foundMetadata.id!!, value, context, comparator,
                    valuePath = "value.name", defaultValuePath = "defaultValue.name")
            else -> throw NotImplementedError()
        }
    }

    private fun createComparisonQuery(property: String, queryComparator: Comparator, value: String, metadataList: List<Metadata>,
                                      context: QueryContext, inverse: Boolean = false): String {
        val foundMetadata =
                metadataList.getByName(property) ?: throw MetadataNotValidException(property)

        val comparator = if (inverse) queryComparator.inverse() else queryComparator
        return when (foundMetadata.type) {
            FLOAT -> valueQueryOrDefault(foundMetadata.id!!, value.toDouble(), context, comparator)
            NUMBER, RANGE -> valueQueryOrDefault(foundMetadata.id!!, value.toInt(), context, comparator)
            DURATION -> valueQueryOrDefault(foundMetadata.id!!, value.toDuration(), context, comparator)
            TAGLIST -> arraySizeQueryOrDefault(foundMetadata.id!!, comparator, value.toInt(), context)
            else -> throw NotImplementedError()
        }
    }

    private fun createTextQuery(text: String, metadataList: List<Metadata>, context: QueryContext,
                                inverse: Boolean = false): String {
        val searchableMetadata = metadataList.textSearchable
        val queries = searchableMetadata.map { textQueryOrDefault(it.id!!, text, context, false, inverse) }
        val comparator = if (inverse) "NOT LIKE" else "LIKE"
        val nameQuery = "$VIDEO_MODEL_NAME.name $comparator ?${context.newVar("%$text%")}"
        return (queries + nameQuery).joinToString(separator = if (inverse) " AND " else " OR ", prefix = "(", postfix = ")")
    }

    private fun arraySizeQueryOrDefault(metadataId: Int, comparator: Comparator, size: Int, context: QueryContext): String {
        val jsonProp = "$metadataId.value"
        return or(
                jsonArraySize(VIDEO_MODEL_NAME, comparator.toDB(), size, context, jsonProp),
                and(
                        or(jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                                jsonArraySize(VIDEO_MODEL_NAME, EQUALS.toDB(), 0, context, jsonProp)).toString(),
                        metadataDefaultValueCheck(metadataId) {
                            jsonArraySize(it, comparator.toDB(), size, context, property = "defaultValue", column = "options")
                        }
                ).toString()
        ).toString()
    }

    private fun metadataDefaultValueCheck(metadataId: Int, check: (String) -> String): String {
        val checkValue = check(METADATA_MODEL_NAME)
        return "EXISTS(SELECT $METADATA_MODEL_NAME.id FROM $METADATA_TABLE_NAME $METADATA_MODEL_NAME WHERE $METADATA_MODEL_NAME.id = $metadataId AND $checkValue)"
    }

    private fun arrayContainsQueryOrDefault(metadataId: Int, value: String, context: QueryContext,
                                            contained: Boolean = true): String {
        val jsonProp = "$metadataId.value"
        return or(
                tagValueContainsQuery(VIDEO_MODEL_NAME, value, context, contained, jsonProp),
                and(
                        jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                        metadataDefaultValueCheck(metadataId) {
                            tagValueContainsQuery(it, value, context, contained, property = "defaultValue", column = "options")
                        }
                ).toString()
        ).toString()
    }

    private fun booleanQueryOrDefault(metadataId: Int, booleanValue: Boolean, context: QueryContext): String {
        val jsonProp = "$metadataId.value"
        return or(
                booleanValueQuery(VIDEO_MODEL_NAME, booleanValue, context, jsonProp),
                and(
                        jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                        metadataDefaultValueCheck(metadataId) {
                            jsonValueMatch(it, booleanValue, context, property = "defaultValue", column = "options")
                        }
                ).toString()
        ).toString()
    }

    private fun valueQueryOrDefault(metadataId: Int, value: Any?, context: QueryContext, comparator: Comparator = EQUALS,
                                    valuePath: String = "value",
                                    defaultValuePath: String = "defaultValue"):
            String {
        val jsonProp = "$metadataId.$valuePath"
        return or(
                jsonValueMatch(VIDEO_MODEL_NAME, value, context, jsonProp, operator = comparator.toDB()),
                and(
                        jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                        metadataDefaultValueCheck(metadataId) {
                            jsonValueMatch(it, value, context, defaultValuePath, column = "options", operator = comparator.toDB())
                        }
                ).toString()
        ).toString()
    }

    private fun textQueryOrDefault(metadataId: Int, value: String, context: QueryContext, exact: Boolean = false,
                                   inverse: Boolean = false): String {
        val jsonProp = "$metadataId.value"
        return or(
                textValueQuery(VIDEO_MODEL_NAME, value, context, exact, inverse, jsonProp),
                and(
                        jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                        metadataDefaultValueCheck(metadataId) {
                            textValueQuery(it, value, context, exact, property = "defaultValue", column = "options")
                        }
                ).toString()
        ).toString()
    }

    private fun Comparator.toDB(): String {
        return when (this) {
            GREATER -> ">"
            SMALLER -> "<"
            GREATER_OR_EQUALS -> ">="
            SMALLER_OR_EQUALS -> "<="
            EQUALS -> "="
            NOT_EQUALS -> "<>"
        }
    }

    private fun tagValueContainsQuery(table: String, value: String, context: QueryContext, isContained: Boolean = true,
                                      property: String = "value",
                                      column: String = "metadata_values") =
            jsonArrayContains(table, property, value, context, isContained, column = column)

    private fun textValueQuery(table: String, value: String, context: QueryContext, exact: Boolean = false,
                               inverse: Boolean = false,
                               property: String = "value", column: String = "metadata_values"): String {
        return when (exact) {
            false -> jsonValueMatch(table, "%$value%", context, property, if (inverse) "NOT LIKE" else "LIKE", column = column)
            true -> jsonValueMatch(table, value, context, property, if (inverse) "!=" else "=", column = column)
        }
    }

    private fun booleanValueQuery(table: String, value: Boolean, context: QueryContext, property: String = "value") =
            jsonValueMatch(table, if (value) 1 else 0, context, property)

    private fun jsonNullMatch(table: String, context: QueryContext, property: String = "value",
                              column: String = "metadata_values") =
            jsonValueMatch(table, null, context, property, "IS", column)

    private fun jsonValueMatch(table: String, value: Any?, context: QueryContext, property: String = "value",
                               operator: String = "=",
                               column: String = "metadata_values"): String {
        return "json_extract($table.$column, '$.$property') $operator ?${context.newVar(value)}"
    }

    private fun jsonArrayContains(table: String, property: String, value: Any?, context: QueryContext,
                                  isContained: Boolean = true,
                                  operator: String = "=", column: String = "metadata_values"): String {
        val queryType = if (!isContained) "NOT EXISTS" else "EXISTS"
        val iterator = "json_each(json_extract($table.$column, '$.$property'))"
        return "$queryType(SELECT * FROM $iterator WHERE json_each.value $operator ?${context.newVar(value)})"
    }

    private fun jsonArraySize(table: String, comparator: String, value: Number, context: QueryContext, property: String = "value",
                              column: String = "metadata_values"): String {
        return "json_array_length($table.$column, '$.$property') $comparator ?${context.newVar(value)}"
    }

    private fun String.toDuration(): Long {
        val durationMatch = DURATION_PATTERN.matchEntire(this) ?: throw IllegalArgumentException("Cannot parse duration")
        val values = durationMatch.groupValues
        val hourString = values[2]
        val minuteString = values[4]
        val secondString = values[6]
        if(hourString.isEmpty() && minuteString.isEmpty() && secondString.isEmpty()) {
            throw IllegalArgumentException("Cannot parse duration")
        }

        val hours = if(hourString.isEmpty()) 0 else hourString.toLong()
        val minutes = if(minuteString.isEmpty()) 0 else minuteString.toLong()
        val seconds = if(secondString.isEmpty()) 0 else secondString.toLong()
        return Duration.of(hours, ChronoUnit.HOURS).plus(minutes, ChronoUnit.MINUTES).plus(seconds, ChronoUnit.SECONDS).seconds
    }

    private fun List<Metadata>.getByName(name: String): Metadata? {
        return this.find { it.name == name }
    }

    private val List<Metadata>.textSearchable
        get() = this.filter { it.type == TEXT }

    companion object {
        private const val TABLE_NAME = "Video"
        private const val VIDEO_MODEL_NAME = "v"
        private const val METADATA_TABLE_NAME = "Metadata"
        private const val METADATA_MODEL_NAME = "m"

        private val DURATION_PATTERN = "((\\d+)h)? ?((\\d+)m)? ?((\\d+)s?)?".toRegex()
    }
}
