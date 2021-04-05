package de.eternalwings.vima.query

import org.springframework.stereotype.Component

@Component
class SearchShorthandProvider {

    private var shorthands = mapOf<String, String>()

    fun hasShorthandConfigured(name: String): Boolean {
        return PREDEFINED_SHORTHANDS.containsKey(name) || shorthands.containsKey(name)
    }

    fun getReplacementForShorthand(name: String): String {
        return PREDEFINED_SHORTHANDS[name] ?: shorthands[name]
        ?: throw IllegalArgumentException("No replacement found for shorthand $name")
    }

    fun registerShorthand(name: String, replacement: String) {
        shorthands = shorthands + (name to replacement)
    }

    companion object {
        private val PREDEFINED_SHORTHANDS = mapOf<String, String>()
    }
}
