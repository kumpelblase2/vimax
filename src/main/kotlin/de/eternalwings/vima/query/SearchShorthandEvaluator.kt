package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.springframework.stereotype.Component

@Component
class SearchShorthandEvaluator(private val shorthandProvider: SearchShorthandProvider) {

    fun handleQuery(query: FullQuery): FullQuery {
        val updated = handleQueryPart(query.part)
        return FullQuery(updated)
    }

    fun handleQueryPart(part: QueryPart): QueryPart {
        return when (part) {
            is IntersectionQuery -> IntersectionQuery(part.parts.map { handleQueryPart(it) })
            is UnionQuery -> UnionQuery(part.parts.map { handleQueryPart(it) })
            is BooleanQuery -> BooleanQuery(handleQueryPart(part.query), part.value)
            is TextQuery -> replaceShorthandsIn(part.text)
            else -> {
                part
            }
        }
    }

    private fun replaceShorthandsIn(text: String): QueryPart {
        if(!shorthandProvider.hasShorthandConfigured(text)) {
            return TextQuery(text)
        }

        val replacement = shorthandProvider.getReplacementForShorthand(text)
        val replacedQuery = QueryParser.parseToEnd(replacement)
        return replacedQuery.part
    }

}
