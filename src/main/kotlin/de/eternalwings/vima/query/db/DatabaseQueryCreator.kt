package de.eternalwings.vima.query.db

import de.eternalwings.vima.query.FullQuery
import de.eternalwings.vima.query.QueryPart

interface DatabaseQueryCreator {
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
    fun createQueryFrom(query: FullQuery): Pair<String, QueryContext>
}
