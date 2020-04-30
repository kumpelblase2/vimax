package de.eternalwings.vima.query

data class FullQuery(var part: QueryPart)

interface QueryPart

data class IntersectionQuery(val parts: List<QueryPart>) : QueryPart
data class UnionQuery(val parts: List<QueryPart>) : QueryPart
data class TextQuery(val text: String) : QueryPart
data class BooleanQuery(val query: QueryPart, val value: Boolean) : QueryPart
data class BooleanOp(val value: Boolean)
data class PropertyQuery(val property: String, val value: String, val like: Boolean = false) : QueryPart
data class ComparisonQuery(val property: String, val comparator: Comparator, val value: String) : QueryPart

enum class Comparator {
    SMALLER,
    GREATER,
    SMALLER_OR_EQUALS,
    GREATER_OR_EQUALS,
    EQUALS,
    NOT_EQUALS,
    LIKE,
    NOT_LIKE,
    IS,
    IS_NOT,
    EXISTS,
    NOT_EXISTS;

    fun inverse(): Comparator = when (this) {
        GREATER -> SMALLER_OR_EQUALS
        SMALLER -> GREATER_OR_EQUALS
        SMALLER_OR_EQUALS -> GREATER
        GREATER_OR_EQUALS -> SMALLER
        EQUALS -> NOT_EQUALS
        NOT_EQUALS -> EQUALS
        IS -> IS_NOT
        IS_NOT -> IS
        LIKE -> NOT_LIKE
        NOT_LIKE -> LIKE
        EXISTS -> NOT_EXISTS
        NOT_EXISTS -> EXISTS
    }

    fun toDB(): String {
        return when (this) {
            GREATER -> ">"
            SMALLER -> "<"
            GREATER_OR_EQUALS -> ">="
            SMALLER_OR_EQUALS -> "<="
            EQUALS -> "="
            NOT_EQUALS -> "<>"
            else -> this.name.replace("_", " ")
        }
    }
}
