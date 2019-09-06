package de.eternalwings.vima.query

import de.eternalwings.vima.query.Filter.AndFilter
import de.eternalwings.vima.query.Filter.OrFilter

sealed class Filter {
    data class AndFilter(val parts: List<String>) : Filter() {
        override fun toString(): String {
            return parts.joinToString(" AND ", "(", ")")
        }
    }

    data class OrFilter(val parts: List<String>): Filter() {
        override fun toString(): String {
            return parts.joinToString(" OR ", "(", ")")
        }
    }
}

fun and(vararg parts: String): Filter {
    return AndFilter(listOf(*parts))
}

fun or(vararg parts: String): Filter {
    return OrFilter(listOf(*parts))
}
