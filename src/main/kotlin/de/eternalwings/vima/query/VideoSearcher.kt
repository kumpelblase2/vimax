package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parsed
import de.eternalwings.vima.domain.Video
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Component
class VideoSearcher(@PersistenceContext private val entityManager: EntityManager,
                    private val videoRepository: VideoRepository,
                    private val databaseQueryCreator: DatabaseQueryCreator) {
    fun search(searchString: String): List<Video> {
        val userQueryAst = when (val userQuery = QueryParser.tryParseToEnd(searchString)) {
            is Parsed<FullQuery> -> userQuery.value
            else -> throw IllegalStateException()
        }

        val finalQuery = this.databaseQueryCreator.createQueryFrom(userQueryAst)
        val dbQuery = entityManager.createNativeQuery(finalQuery)

        val resultStream = dbQuery.resultStream as Stream<Int>
        return videoRepository.findAllById(resultStream.collect(Collectors.toList()))
    }
}
