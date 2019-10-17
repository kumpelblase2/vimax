package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parsed
import org.springframework.stereotype.Component
import java.util.stream.Stream
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.streams.toList

@Component
class VideoSearcher(@PersistenceContext private val entityManager: EntityManager,
                    private val databaseQueryCreator: DatabaseQueryCreator) {
    fun search(searchString: String): List<Int> {
        val userQueryAst = when (val userQuery = QueryParser.tryParseToEnd(searchString)) {
            is Parsed<FullQuery> -> userQuery.value
            else -> throw IllegalStateException()
        }

        val finalQuery = this.databaseQueryCreator.createQueryFrom(userQueryAst)
        val dbQuery = entityManager.createNativeQuery(finalQuery)

        return (dbQuery.resultStream as Stream<Int>).toList()
    }
}
