package de.eternalwings.vima.query.db

import de.eternalwings.vima.query.Comparator
import de.eternalwings.vima.query.Comparator.EQUALS
import de.eternalwings.vima.query.Comparator.EXISTS
import de.eternalwings.vima.query.Comparator.IS
import de.eternalwings.vima.query.Comparator.LIKE
import de.eternalwings.vima.query.Comparator.SMALLER_OR_EQUALS
import de.eternalwings.vima.query.Filter
import de.eternalwings.vima.query.Filter.ContainerQuery
import de.eternalwings.vima.query.Filter.PropertyFilter
import de.eternalwings.vima.query.Filter.WrappedFilter
import de.eternalwings.vima.query.and
import de.eternalwings.vima.query.db.DatabaseQueryCreator.QueryContext
import de.eternalwings.vima.query.or
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DatabaseQueryCreatorSqlite(
    metadataRepository: MetadataRepository,
    @Value("\${duration-query-tolerance:3}") durationTolerance: Long
) : BaseDatabaseQueryCreator(metadataRepository, durationTolerance) {

    override fun arraySizeQueryOrDefault(metadataId: Int, comparator: Comparator, size: Int, context: QueryContext): Filter {
        val jsonProp = "$metadataId.value"
        return or(
            jsonArraySize(VIDEO_MODEL_NAME, comparator, size, context, jsonProp),
            and(
                or(
                    jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                    jsonArraySize(VIDEO_MODEL_NAME, EQUALS, 0, context, jsonProp)
                ),
                metadataDefaultValueCheck(metadataId) {
                    jsonArraySize(it, comparator, size, context, property = "defaultValue", column = "options")
                }
            )
        )
    }

    private fun metadataDefaultValueCheck(metadataId: Int, check: (String) -> Filter): Filter {
        val checkValue = check(METADATA_MODEL_NAME)
        // We don't need this EXISTS to flip in queries!
        return WrappedFilter(
            "EXISTS(SELECT $METADATA_MODEL_NAME.id FROM $METADATA_TABLE_NAME $METADATA_MODEL_NAME WHERE " +
                    "$METADATA_MODEL_NAME.id = $metadataId AND ", checkValue
        )
    }

    override fun arrayContainsQueryOrDefault(metadataId: Int, value: String, like: Boolean, context: QueryContext): Filter {
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

    override fun booleanQueryOrDefault(metadataId: Int, booleanValue: Boolean, context: QueryContext): Filter {
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

    override fun valueQueryOrDefault(
        metadataId: Int,
        value: Any?,
        context: QueryContext,
        comparator: Comparator,
        valuePath: String,
        defaultValuePath: String
    ): Filter {
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

    override fun textQueryOrDefault(metadataId: Int, value: String, context: QueryContext, exact: Boolean): Filter {
        val jsonProp = "$metadataId.value"
        val textQuery = if (value.isEmpty()) {
            or(
                textValueQuery(VIDEO_MODEL_NAME, value, context, exact, jsonProp),
                jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp)
            )
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

    override fun durationQueryOrDefault(metadataId: Int, value: Long, tolerance: Long, context: QueryContext): Filter {
        val jsonProp = "$metadataId.value"
        return or(
            jsonDifferenceMatch(VIDEO_MODEL_NAME, value, tolerance, context, jsonProp),
            and(
                jsonNullMatch(VIDEO_MODEL_NAME, context, jsonProp),
                metadataDefaultValueCheck(metadataId) {
                    jsonDifferenceMatch(
                        it,
                        value,
                        tolerance,
                        context,
                        property = "defaultValue",
                        column = "options"
                    )
                }
            )
        )
    }

    private fun tagValueContainsQuery(
        table: String, value: String, like: Boolean, context: QueryContext,
        property: String = "value",
        column: String = "metadata_values"
    ) =
        jsonArrayContains(
            table, property, if (like) "%$value%" else value, context, column = column,
            operator = if (like) LIKE else EQUALS
        )

    private fun textValueQuery(
        table: String, value: String, context: QueryContext, exact: Boolean = false,
        property: String = "value", column: String = "metadata_values"
    ): Filter {
        return when (exact) {
            false -> jsonValueMatch(table, "%$value%", context, property, LIKE, column = column)
            true -> jsonValueMatch(table, value, context, property, column = column)
        }
    }

    private fun booleanValueQuery(table: String, value: Boolean, context: QueryContext, property: String = "value") =
        jsonValueMatch(table, if (value) 1 else 0, context, property)

    private fun jsonNullMatch(
        table: String, context: QueryContext, property: String = "value",
        column: String = "metadata_values"
    ) =
        jsonValueMatch(table, null, context, property, IS, column)

    private fun jsonValueMatch(
        table: String, value: Any?, context: QueryContext, property: String = "value",
        operator: Comparator = EQUALS,
        column: String = "metadata_values"
    ): Filter {
        return PropertyFilter("json_extract($table.$column, '$.$property')", operator, "?${context.newVar(value)}")
    }

    private fun jsonDifferenceMatch(
        table: String,
        value: Any?,
        difference: Long,
        context: QueryContext,
        property: String = "value",
        column: String = "metadata_values"
    ): Filter {
        return PropertyFilter(
            "abs(json_extract($table.$column, '$.$property') - ?${context.newVar(value)})",
            SMALLER_OR_EQUALS,
            "?${context.newVar(difference)}"
        )
    }

    private fun jsonArrayContains(
        table: String, property: String, value: Any?, context: QueryContext,
        operator: Comparator = EQUALS, column: String = "metadata_values"
    ): Filter {
        val iterator = "json_each(json_extract($table.$column, '$.$property'))"
        return ContainerQuery(
            EXISTS,
            "SELECT * FROM $iterator WHERE json_each.value ${operator.toDB()} ?${context.newVar(value)}"
        )
    }

    private fun jsonArraySize(
        table: String, comparator: Comparator, value: Number, context: QueryContext,
        property: String = "value",
        column: String = "metadata_values"
    ): Filter {
        return PropertyFilter("json_array_length($table.$column, '$.$property')", comparator, "?${context.newVar(value)}")
    }

    companion object {
        private const val VIDEO_MODEL_NAME = "v"
        private const val METADATA_TABLE_NAME = "Metadata"
        private const val METADATA_MODEL_NAME = "m"
    }
}
