package de.eternalwings.vima.query

import org.springframework.stereotype.Component

@Component
class SearchShorthandProvider {
    data class ShorthandDefinition(val name: String, val ownerId: Int?)

    private var shorthands = mapOf<ShorthandDefinition, String>()

    fun hasShorthandConfigured(name: String): Boolean {
        return shorthands.keys.any { it.name == name }
    }

    fun getReplacementForShorthand(name: String): String {
        val shorthands = shorthands.entries.filter { it.key.name == name }
        if (shorthands.isEmpty()) {
            throw IllegalArgumentException("No replacement found for shorthand $name")
        }

        if (shorthands.size > 2 || (shorthands.size > 1 && shorthands.none { it.key.ownerId == null })) {
            throw IllegalArgumentException("Too many replacements to chose from for $name")
        }

        return if (shorthands.size == 1) {
            shorthands[0].value
        } else {
            shorthands.find { it.key.ownerId != null }!!.value
        }
    }

    fun registerShorthand(name: String, replacement: String, ownerId: Int? = null) {
        shorthands = shorthands + (ShorthandDefinition(name, ownerId) to replacement)
    }

    fun unregisterShorthandsOf(ownerId: Int) {
        shorthands = shorthands.filterKeys { it.ownerId != ownerId }
    }
}
