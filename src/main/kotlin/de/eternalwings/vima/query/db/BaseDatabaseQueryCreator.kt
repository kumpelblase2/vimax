package de.eternalwings.vima.query.db

import de.eternalwings.vima.MetadataType.BOOLEAN
import de.eternalwings.vima.MetadataType.DURATION
import de.eternalwings.vima.MetadataType.FLOAT
import de.eternalwings.vima.MetadataType.NUMBER
import de.eternalwings.vima.MetadataType.RANGE
import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.MetadataType.TAGLIST
import de.eternalwings.vima.MetadataType.TEXT
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.domain.SelectionMetadataOptions
import de.eternalwings.vima.ext.tolerance
import de.eternalwings.vima.query.BooleanQuery
import de.eternalwings.vima.query.Comparator
import de.eternalwings.vima.query.Comparator.EQUALS
import de.eternalwings.vima.query.Comparator.LIKE
import de.eternalwings.vima.query.Comparator.NOT_EQUALS
import de.eternalwings.vima.query.ComparisonQuery
import de.eternalwings.vima.query.Filter
import de.eternalwings.vima.query.Filter.PropertyFilter
import de.eternalwings.vima.query.FullQuery
import de.eternalwings.vima.query.IntersectionQuery
import de.eternalwings.vima.query.PropertyQuery
import de.eternalwings.vima.query.QueryPart
import de.eternalwings.vima.query.TextQuery
import de.eternalwings.vima.query.UnionQuery
import de.eternalwings.vima.query.and
import de.eternalwings.vima.query.db.DatabaseQueryCreator.InvalidQueryOperationException
import de.eternalwings.vima.query.db.DatabaseQueryCreator.MetadataNotValidException
import de.eternalwings.vima.query.db.DatabaseQueryCreator.QueryContext
import de.eternalwings.vima.query.or
import de.eternalwings.vima.repository.MetadataRepository
import java.time.Duration
import java.time.temporal.ChronoUnit

abstract class BaseDatabaseQueryCreator(protected val metadataRepository: MetadataRepository) : DatabaseQueryCreator {
    @Throws(MetadataNotValidException::class)
    override fun createQueryFrom(query: FullQuery): Pair<String, QueryContext> {
        val allMetadata = this.metadataRepository.findAll()
        return createQueryFrom(query.part, allMetadata)
    }

    protected fun createQueryFrom(queryPart: QueryPart, metadataList: List<Metadata>): Pair<String, QueryContext> {
        val start = "SELECT $VIDEO_MODEL_NAME.id FROM $TABLE_NAME $VIDEO_MODEL_NAME WHERE "
        val context = QueryContext()
        return (start + createQueryCondition(queryPart, metadataList, context).toString()) to context
    }

    protected fun createQueryCondition(queryPart: QueryPart, metadataList: List<Metadata>, context: QueryContext): Filter {
        return when (queryPart) {
            is UnionQuery -> or(queryPart.parts.map { createQueryCondition(it, metadataList, context) })
            is IntersectionQuery -> and(queryPart.parts.map { createQueryCondition(it, metadataList, context) })
            is PropertyQuery -> createPropertyQuery(queryPart.property, queryPart.value, metadataList, queryPart.like, context)
            is BooleanQuery -> createBooleanQuery(queryPart, metadataList, context)
            is TextQuery -> createTextQuery(queryPart.text, metadataList, context)
            is ComparisonQuery -> createComparisonQuery(
                queryPart.property, queryPart.comparator, queryPart.value, metadataList,
                context
            )
            else -> throw InvalidQueryOperationException(queryPart)
        }
    }

    protected fun createBooleanQuery(booleanQuery: BooleanQuery, metadataList: List<Metadata>, context: QueryContext): Filter {
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

    protected fun createPropertyQuery(
        property: String, value: String, metadataList: List<Metadata>, like: Boolean,
        context: QueryContext
    ): Filter {
        val foundMetadata =
            metadataList.getByName(property) ?: throw MetadataNotValidException(property)

        return when (foundMetadata.type) {
            BOOLEAN -> booleanQueryOrDefault(foundMetadata.id!!, value.toBoolean(), context)
            TAGLIST -> arrayContainsQueryOrDefault(foundMetadata.id!!, value, like, context)
            TEXT -> textQueryOrDefault(foundMetadata.id!!, value, context, !like)
            FLOAT -> valueQueryOrDefault(foundMetadata.id!!, value.toFloat(), context)
            NUMBER, RANGE -> valueQueryOrDefault(foundMetadata.id!!, value.toInt(), context)
            SELECTION -> {
                val foundId = (foundMetadata.options as SelectionMetadataOptions).values.find { it.name == value }?.id
                    ?: throw RuntimeException("Specified selection value does not exist.")
                valueQueryOrDefault(foundMetadata.id!!, foundId, context, defaultValuePath = "defaultValue.id")
            }
            DURATION -> {
                val duration = value.toDuration()
                val tolerance = if(like) duration.tolerance(3) else 0
                durationQueryOrDefault(foundMetadata.id!!, duration.toSeconds(), tolerance, context)
            }
            else -> throw NotImplementedError()
        }
    }

    protected fun createComparisonQuery(
        property: String, queryComparator: Comparator, value: String, metadataList: List<Metadata>,
        context: QueryContext
    ): Filter {
        val foundMetadata =
            metadataList.getByName(property) ?: throw MetadataNotValidException(property)
        return when (foundMetadata.type) {
            FLOAT -> valueQueryOrDefault(foundMetadata.id!!, value.toDouble(), context, queryComparator)
            NUMBER, RANGE -> valueQueryOrDefault(foundMetadata.id!!, value.toInt(), context, queryComparator)
            DURATION -> valueQueryOrDefault(foundMetadata.id!!, value.toDuration().toSeconds(), context, queryComparator)
            TAGLIST -> arraySizeQueryOrDefault(foundMetadata.id!!, queryComparator, value.toInt(), context)
            else -> throw NotImplementedError()
        }
    }

    protected fun createTextQuery(text: String, metadataList: List<Metadata>, context: QueryContext): Filter {
        val searchableMetadata = metadataList.textSearchable
        val queries = searchableMetadata.map { textQueryOrDefault(it.id!!, text, context, false) }
        val nameQuery = PropertyFilter("$VIDEO_MODEL_NAME.name", LIKE, "?${context.newVar("%$text%")}")
        return or(queries + nameQuery)
    }

    protected abstract fun arraySizeQueryOrDefault(
        metadataId: Int,
        comparator: Comparator,
        size: Int,
        context: QueryContext
    ): Filter

    protected abstract fun arrayContainsQueryOrDefault(
        metadataId: Int,
        value: String,
        like: Boolean,
        context: QueryContext
    ): Filter

    protected abstract fun booleanQueryOrDefault(metadataId: Int, booleanValue: Boolean, context: QueryContext): Filter

    protected abstract fun valueQueryOrDefault(
        metadataId: Int,
        value: Any?,
        context: QueryContext,
        comparator: Comparator = EQUALS,
        valuePath: String = "value",
        defaultValuePath: String = "defaultValue"
    ): Filter

    protected abstract fun textQueryOrDefault(
        metadataId: Int,
        value: String,
        context: QueryContext,
        exact: Boolean = false
    ): Filter

    protected abstract fun durationQueryOrDefault(
        metadataId: Int,
        value: Long,
        tolerance: Long,
        context: QueryContext,
    ): Filter

    protected fun String.toDuration(): Duration {
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
        return Duration.of(hours, ChronoUnit.HOURS).plus(minutes, ChronoUnit.MINUTES).plus(seconds, ChronoUnit.SECONDS)
    }

    protected fun List<Metadata>.getByName(name: String): Metadata? {
        return this.find { it.name == name }
    }

    protected val List<Metadata>.textSearchable
        get() = this.filter { it.type == TEXT }

    companion object {
        protected const val TABLE_NAME = "Video"
        protected const val VIDEO_MODEL_NAME = "v"

        private val DURATION_PATTERN = "((\\d+)h)? ?((\\d+)m)? ?((\\d+)s?)?".toRegex()
    }
}
