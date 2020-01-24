package de.eternalwings.vima.query

import de.eternalwings.vima.query.Filter.AndFilter
import de.eternalwings.vima.query.Filter.OrFilter

sealed class Filter {
    data class AndFilter(val parts: List<Filter>) : Filter() {
        override fun toString(): String {
            return parts.joinToString(" AND ", "(", ")")
        }

        override fun inverse(): Filter {
            return OrFilter(parts.map { it.inverse() })
        }
    }

    data class OrFilter(val parts: List<Filter>): Filter() {
        override fun toString(): String {
            return parts.joinToString(" OR ", "(", ")")
        }

        override fun inverse(): Filter {
            return AndFilter(parts.map { it.inverse() })
        }
    }

    data class PropertyFilter(val propertyPart: String, val comparator: Comparator, val queryPart: Any) : Filter() {
        override fun toString(): String {
            return listOf(propertyPart, comparator.toDB(), queryPart).joinToString(" ")
        }

        override fun inverse(): Filter {
            return PropertyFilter(propertyPart, comparator.inverse(), queryPart)
        }
    }

    data class WrappedFilter(val container: String, val inner: Filter) : Filter() {
        override fun toString(): String {
            return "$container $inner)"
        }

        override fun inverse(): Filter {
            return WrappedFilter(container, inner.inverse())
        }
    }

    data class ContainerQuery(val operator: Comparator, val query: String): Filter() {
        override fun toString(): String {
            return "${operator.toDB()}($query)"
        }

        override fun inverse(): Filter {
            return ContainerQuery(operator.inverse(), query)
        }
    }

    abstract fun inverse() : Filter
}

fun and(vararg parts: Filter): Filter {
    return AndFilter(listOf(*parts))
}

fun or(vararg parts: Filter): Filter {
    return OrFilter(listOf(*parts))
}

fun or(parts: List<Filter>): Filter {
    return OrFilter(parts)
}

fun and(parts: List<Filter>): Filter {
    return AndFilter(parts)
}
