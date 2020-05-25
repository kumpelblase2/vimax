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
import de.eternalwings.vima.query.Comparator.EXISTS
import de.eternalwings.vima.query.Comparator.IS
import de.eternalwings.vima.query.Comparator.LIKE
import de.eternalwings.vima.query.Comparator.NOT_EQUALS
import de.eternalwings.vima.query.Filter.ContainerQuery
import de.eternalwings.vima.query.Filter.PropertyFilter
import de.eternalwings.vima.query.Filter.WrappedFilter
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
        return (start + createQueryCondition(queryPart, metadataList, context).toString()) to context
    }

    private fun createQueryCondition(queryPart: QueryPart, metadataList: List<Metadata>, context: QueryContext): Filter {
        return when (queryPart) {
            is UnionQuery -> or(queryPart.parts.map { createQueryCondition(it, metadataList, context) })
            is IntersectionQuery -> and(queryPart.parts.map { createQueryCondition(it, metadataList, context) })
            is PropertyQuery -> createPropertyQuery(queryPart.property, queryPart.value, metadataList, queryPart.like, context)
            is BooleanQuery -> createBooleanQuery(queryPart, metadataList, context)
            is TextQuery -> createTextQuery(queryPart.text, metadataList, context)
            is ComparisonQuery -> createComparisonQuery(queryPart.property, queryPart.comparator, queryPart.value, metadataList,
                    context)
            else -> throw InvalidQueryOperationException(queryPart)
        }
    }

    private fun createBooleanQuery(booleanQuery: BooleanQuery, metadataList: List<Metadata>, context: QueryContext): Filter {
        val query = when (val inner = booleanQuery.query) {
            is TextQuery -> {
                val propertyName = inner.text
                val property = metadataList.getByName(propertyName)
                if (property != null) {
                    when (property.type) {
                        TAGLIST -> createComparisonQuery(propertyName, NOT_EQUALS, "0", metadataList, context)
                        else -> createPropertyQuery(propertyName, "", metadataList, false, context).inverse()
                    }
                } else {
                    createTextQuery(propertyName, metadataList, context)
                }
            }
            is PropertyQuery -> createPropertyQuery(inner.property, inner.value, metadataList, inner.like, context)
            is ComparisonQuery -> createComparisonQuery(inner.property, inner.comparator, inner.value, metadataList, context)
            else -> throw InvalidQueryOperationException(inner)
        }

        return if (!booleanQuery.value) query.inverse() else query
    }

    private fun createPropertyQuery(property: String, value: String, metadataList: List<Metadata>, like: Boolean,
                                    context: QueryContext): Filter {
        val foundMetadata =
                metadataList.getByName(property) ?: throw MetadataNotValidException(property)

        return when (foundMetadata.type) {
            BOOLEAN -> booleanQueryOrDefault(foundMetadata.id!!, value.toBoolean(), context)
            TAGLIST -> arrayContainsQueryOrDefault(foundMetadata.id!!, value, like, context)
            TEXT -> textQueryOrDefault(foundMetadata.id!!, value, context, !like)
            FLOAT -> valueQueryOrDefault(foundMetadata.id!!, value.toFloat(), context)
            NUMBER, RANGE -> valueQueryOrDefault(foundMetadata.id!!, value.toInt(), context)
            SELECTION -> valueQueryOrDefault(foundMetadata.id!!, value, context, valuePath = "value.name",
                    defaultValuePath = "defaultValue.name")
            else -> throw NotImplementedError()
        }
    }

    private fun createComparisonQuery(property: String, queryComparator: Comparator, value: String, metadataList: List<Metadata>,
                                      context: QueryContext): Filter {
        val foundMetadata =
                metadataList.getByName(property) ?: throw MetadataNotValidException(property)
        return when (foundMetadata.type) {
            FLOAT -> valueQueryOrDefault(foundMetadata.id!!, value.toDouble(), context, queryComparator)
            NUMBER, RANGE -> valueQueryOrDefault(foundMetadata.id!!, value.toInt(), context, queryComparator)
            DURATION -> valueQueryOrDefault(foundMetadata.id!!, value.toDuration(), context, queryComparator)
            TAGLIST -> arraySizeQueryOrDefault(foundMetadata.id!!, queryComparator, value.toInt(), context)
            else -> throw NotImplementedError()
        }
    }

    private fun createTextQuery(text: String, metadataList: List<Metadata>, context: QueryContext): Filter {
        val searchableMetadata = metadataList.textSearchable
        val queries = searchableMetadata.map { textQueryOrDefault(it.id!!, text, context, false) }
        val nameQuery = PropertyFilter("$VIDEO_MODEL_NAME.name", LIKE, "?${context.newVar("%$text%")}")
        return or(queries + nameQuery)
    }

    private fun arraySizeQueryOrDefault(metadataId: Int, comparator: Comparator, size: Int, context: QueryContext): Filter {
        val jsonProp = "$metadataId.value"
        return or(
                jsonArraySize(VIDEO_MODEL_NAME, comparator, size, context, jsonProp),
                and(
                        or(jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                                jsonArraySize(VIDEO_MODEL_NAME, EQUALS, 0, context, jsonProp)),
                        metadataDefaultValueCheck(metadataId) {
                            jsonArraySize(it, comparator, size, context, property = "defaultValue", column = "options")
                        }
                )
        )
    }

    private fun metadataDefaultValueCheck(metadataId: Int, check: (String) -> Filter): Filter {
        val checkValue = check(METADATA_MODEL_NAME)
        // We don't need this EXISTS to flip in queries!
        return WrappedFilter("EXISTS(SELECT $METADATA_MODEL_NAME.id FROM $METADATA_TABLE_NAME $METADATA_MODEL_NAME WHERE " +
                "$METADATA_MODEL_NAME.id = $metadataId AND ", checkValue)
    }

    private fun arrayContainsQueryOrDefault(metadataId: Int, value: String, like: Boolean, context: QueryContext): Filter {
        val jsonProp = "$metadataId.value"
        return or(
                tagValueContainsQuery(VIDEO_MODEL_NAME, value, like, context, jsonProp),
                and(
                        jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                        metadataDefaultValueCheck(metadataId) {
                            tagValueContainsQuery(it, value, like, context, property = "defaultValue", column = "options")
                        }
                )
        )
    }

    private fun booleanQueryOrDefault(metadataId: Int, booleanValue: Boolean, context: QueryContext): Filter {
        val jsonProp = "$metadataId.value"
        val booleanCheck = if (booleanValue) {
            booleanValueQuery(VIDEO_MODEL_NAME, booleanValue, context, jsonProp)
        } else {
            or(
                    booleanValueQuery(VIDEO_MODEL_NAME, booleanValue, context, jsonProp),
                    jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp)
            )
        }

        return or(
                booleanCheck,
                and(
                        jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                        metadataDefaultValueCheck(metadataId) {
                            jsonValueMatch(it, booleanValue, context, property = "defaultValue", column = "options")
                        }
                )
        )
    }

    private fun valueQueryOrDefault(metadataId: Int, value: Any?, context: QueryContext, comparator: Comparator = EQUALS,
                                    valuePath: String = "value",
                                    defaultValuePath: String = "defaultValue"):
            Filter {
        val jsonProp = "$metadataId.$valuePath"
        return or(
                jsonValueMatch(VIDEO_MODEL_NAME, value, context, jsonProp, operator = comparator),
                and(
                        jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                        metadataDefaultValueCheck(metadataId) {
                            jsonValueMatch(it, value, context, defaultValuePath, column = "options", operator = comparator)
                        }
                )
        )
    }

    private fun textQueryOrDefault(metadataId: Int, value: String, context: QueryContext, exact: Boolean = false): Filter {
        val jsonProp = "$metadataId.value"
        val textQuery = if (value.isEmpty()) {
            or(textValueQuery(VIDEO_MODEL_NAME, value, context, exact, jsonProp),
                    jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp))
        } else {
            textValueQuery(VIDEO_MODEL_NAME, value, context, exact, jsonProp)
        }

        return or(textQuery, and(
                jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                metadataDefaultValueCheck(metadataId) {
                    textValueQuery(it, value, context, exact, property = "defaultValue", column = "options")
                }
        ))
    }

    private fun tagValueContainsQuery(table: String, value: String, like: Boolean, context: QueryContext,
                                      property: String = "value",
                                      column: String = "metadata_values") =
            jsonArrayContains(table, property, if (like) "%$value%" else value, context, column = column,
                    operator = if (like) LIKE else EQUALS)

    private fun textValueQuery(table: String, value: String, context: QueryContext, exact: Boolean = false,
                               property: String = "value", column: String = "metadata_values"): Filter {
        return when (exact) {
            false -> jsonValueMatch(table, "%$value%", context, property, LIKE, column = column)
            true -> jsonValueMatch(table, value, context, property, column = column)
        }
    }

    private fun booleanValueQuery(table: String, value: Boolean, context: QueryContext, property: String = "value") =
            jsonValueMatch(table, if (value) 1 else 0, context, property)

    private fun jsonNullMatch(table: String, context: QueryContext, property: String = "value",
                              column: String = "metadata_values") =
            jsonValueMatch(table, null, context, property, IS, column)

    private fun jsonValueMatch(table: String, value: Any?, context: QueryContext, property: String = "value",
                               operator: Comparator = EQUALS,
                               column: String = "metadata_values"): Filter {
        return PropertyFilter("json_extract($table.$column, '$.$property')", operator, "?${context.newVar(value)}")
    }

    private fun jsonArrayContains(table: String, property: String, value: Any?, context: QueryContext,
                                  operator: Comparator = EQUALS, column: String = "metadata_values"): Filter {
        val iterator = "json_each(json_extract($table.$column, '$.$property'))"
        return ContainerQuery(EXISTS,
                "SELECT * FROM $iterator WHERE json_each.value ${operator.toDB()} ?${context.newVar(value)}")
    }

    private fun jsonArraySize(table: String, comparator: Comparator, value: Number, context: QueryContext,
                              property: String = "value",
                              column: String = "metadata_values"): Filter {
        return PropertyFilter("json_array_length($table.$column, '$.$property')", comparator, "?${context.newVar(value)}")
    }

    private fun String.toDuration(): Long {
        val durationMatch = DURATION_PATTERN.matchEntire(this) ?: throw IllegalArgumentException("Cannot parse duration")
        val values = durationMatch.groupValues
        val hourString = values[2]
        val minuteString = values[4]
        val secondString = values[6]
        if (hourString.isEmpty() && minuteString.isEmpty() && secondString.isEmpty()) {
            throw IllegalArgumentException("Cannot parse duration")
        }

        val hours = if (hourString.isEmpty()) 0 else hourString.toLong()
        val minutes = if (minuteString.isEmpty()) 0 else minuteString.toLong()
        val seconds = if (secondString.isEmpty()) 0 else secondString.toLong()
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
