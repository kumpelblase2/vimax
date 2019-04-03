package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parsed
import de.eternalwings.vima.domain.Video
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Component
class VideoSearcher(@PersistenceContext private val entityManager: EntityManager,
                    private val databaseQueryCreator: DatabaseQueryCreator) {
    fun search(searchString: String): List<Video> {
        val query = "SELECT v FROM Video v where "
        val userQuery = QueryParser.tryParseToEnd(searchString)
        val userQueryAst = when (userQuery) {
            is Parsed<FullQuery> -> userQuery.value
            else -> throw IllegalStateException()
        }

        val finalQuery = query + this.databaseQueryCreator.createQueryFrom(userQueryAst, "v")
        val dbQuery = entityManager.createQuery(finalQuery, Video::class.java)

        return dbQuery.resultList
    }
}
