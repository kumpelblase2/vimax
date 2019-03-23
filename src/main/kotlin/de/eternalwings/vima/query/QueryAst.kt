package de.eternalwings.vima.query

data class FullQuery(var part: QueryPart)

interface QueryPart

data class IntersectionQuery(val parts: List<QueryPart>) : QueryPart
data class UnionQuery(val parts: List<QueryPart>) : QueryPart
data class TextQuery(val text: String) : QueryPart
data class BooleanQuery(val query: QueryPart, val value: Boolean) : QueryPart
data class BooleanOp(val value: kotlin.Boolean)
data class PropertyQuery(val property: String, val value: String) : QueryPart
data class ComparisonQuery(val property: String, val comparator: Comparator, val value: String) : QueryPart

enum class Comparator {
    SMALLER,
    LARGER
}
