package de.eternalwings.vima.query

import de.eternalwings.vima.MetadataType.BOOLEAN
import de.eternalwings.vima.MetadataType.FLOAT
import de.eternalwings.vima.MetadataType.NUMBER
import de.eternalwings.vima.MetadataType.RANGE
import de.eternalwings.vima.MetadataType.SELECTION
import de.eternalwings.vima.MetadataType.TAGLIST
import de.eternalwings.vima.MetadataType.TEXT
import de.eternalwings.vima.domain.Metadata
import de.eternalwings.vima.query.Comparator.GREATER
import de.eternalwings.vima.query.Comparator.GREATER_OR_EQUALS
import de.eternalwings.vima.query.Comparator.SMALLER
import de.eternalwings.vima.query.Comparator.SMALLER_OR_EQUALS
import de.eternalwings.vima.repository.MetadataRepository
import org.springframework.stereotype.Component

@Component
class DatabaseQueryCreator(private val metadataRepository: MetadataRepository) {

    class MetadataNotValidException(metadataName: String) :
            RuntimeException("Metadata with name $metadataName does not exist.")

    class InvalidQueryOperationException(val type: QueryPart) : RuntimeException()

    @Throws(MetadataNotValidException::class)
    fun createQueryFrom(query: FullQuery, modelName: String): String {
        val allMetadata = this.metadataRepository.findAll()
        return createQueryFrom(query.part, allMetadata, modelName)
    }

    private fun createQueryFrom(queryPart: QueryPart, metadataList: List<Metadata>, modelId: String): String {
        return when (queryPart) {
            is UnionQuery -> "(" + queryPart.parts.joinToString(separator = " or ") {
                createQueryFrom(it, metadataList, modelId)
            } + ")"
            is IntersectionQuery -> "(" + queryPart.parts.joinToString(separator = " and ") {
                createQueryFrom(it, metadataList, modelId)
            } + ")"
            is PropertyQuery -> createPropertyQuery(queryPart, metadataList, modelId)
            is BooleanQuery -> createBooleanQuery(queryPart, metadataList, modelId)
            is TextQuery -> createTextQuery(queryPart, metadataList, modelId)
            is ComparisonQuery -> createComparisonQuery(queryPart, metadataList, modelId)
            else -> throw InvalidQueryOperationException(queryPart)
        }
    }

    private fun createBooleanQuery(booleanQuery: BooleanQuery, metadataList: List<Metadata>,
                                   modelId: String): String {
        val query = booleanQuery.query
        return when (query) {
            is PropertyQuery -> createPropertyQuery(query, metadataList, modelId, !booleanQuery.value)
            is ComparisonQuery -> createComparisonQuery(query, metadataList, modelId, !booleanQuery.value)
            is TextQuery -> {
                val foundMetadata = metadataList.find { metadata ->
                    metadata.name == query.text
                } ?: throw MetadataNotValidException(query.text)

                return "($modelId.id IN (SELECT mv.video.id FROM FullMetadataValue mv WHERE " +
                        when (foundMetadata.type) {
                            BOOLEAN -> "mv.booleanValue = " + booleanQuery.value
                            else -> ""
                        } + " AND mv.metadata.id = ${foundMetadata.id}))"
            }
            else -> throw NotImplementedError()
        }
    }

    private fun createTextQuery(textQuery: TextQuery, metadataList: List<Metadata>,
                                modelId: String): String {
        val metadataDefSelection = metadataList.asSequence().filter {
            it.type == TEXT
        }.map {
            "mv.metadata.id = ${it.id}"
        }.joinToString(" OR ")

        return "($modelId.id IN (SELECT mv.video.id FROM FullMetadataValue mv WHERE " +
                "mv.stringValue LIKE '%${textQuery.text}%' AND (" + metadataDefSelection + ")))"
    }

    private fun createPropertyQuery(propertyQuery: PropertyQuery, metadataList: List<Metadata>,
                                    modelId: String, inverse: Boolean = false): String {
        val foundMetadata = metadataList.find { metadata -> metadata.name == propertyQuery.property }
                ?: throw MetadataNotValidException(propertyQuery.property)

        return "($modelId.id IN (SELECT mv.video.id FROM FullMetadataValue mv WHERE " + when (foundMetadata.type) {
            SELECTION -> "mv.selectionValue.name" + (if (inverse) " <> " else " = ") + propertyQuery.value
            FLOAT -> "mv.floatingValue" + (if (inverse) " <> " else " = ") + propertyQuery.value
            NUMBER, RANGE -> "mv.numberValue" + (if (inverse) " <> " else " = ") + propertyQuery.value
            TEXT -> "mv.stringValue" + (if (inverse) " <> " else " = ") + "'${propertyQuery.value}'"
            TAGLIST -> "POSITION_ARRAY('${propertyQuery.value}', mv.taglistValues)" + (if (inverse) "= 0" else "> 0")
            BOOLEAN -> "mv.booleanValue" + (if (inverse) " <> " else " = ") + propertyQuery.value
            else -> throw IllegalStateException()
        } + " AND mv.metadata.id = ${foundMetadata.id}))"
    }

    private fun createComparisonQuery(comparisonQuery: ComparisonQuery, metadataList: List<Metadata>,
                                      modelId: String, inverse: Boolean = false): String {
        val foundMetadata = metadataList.find { metadata -> metadata.name == comparisonQuery.property }
                ?: throw MetadataNotValidException(comparisonQuery.property)

        val comparator = if (inverse) comparisonQuery.comparator.inverse() else comparisonQuery.comparator
        return "($modelId.id IN (SELECT mv.video.id FROM FullMetadataValue mv WHERE " + when (foundMetadata.type) {
            FLOAT -> "mv.floatingValue " + comparator.toDB() + comparisonQuery.value
            NUMBER, RANGE -> "mv.numberValue " + comparator.toDB() + comparisonQuery.value
            TAGLIST -> "CARDINALITY(mv.taglistValues) " + comparator.toDB() + comparisonQuery.value
            else -> throw IllegalStateException()
        } + " AND mv.metadata.id = ${foundMetadata.id}))"
    }

    private fun Comparator.toDB(): String {
        return when (this) {
            GREATER -> ">"
            SMALLER -> "<"
            GREATER_OR_EQUALS -> ">="
            SMALLER_OR_EQUALS -> "<="
        }
    }
}
