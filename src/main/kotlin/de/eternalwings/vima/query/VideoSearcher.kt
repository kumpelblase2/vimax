package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parsed
import de.eternalwings.vima.query.db.DatabaseQueryCreator
import de.eternalwings.vima.repository.MetadataRepository
import de.eternalwings.vima.repository.VideoRepository
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.stereotype.Component
import java.util.stream.Stream
import javax.persistence.EntityManager
import javax.persistence.EntityNotFoundException
import javax.persistence.PersistenceContext
import kotlin.streams.toList

@Component
class VideoSearcher(@PersistenceContext private val entityManager: EntityManager,
                    private val videoRepository: VideoRepository,
                    private val metadataRepository: MetadataRepository,
                    private val searchShorthandEvaluator: SearchShorthandEvaluator,
                    private val databaseQueryCreator: DatabaseQueryCreator) {
    private fun query(searchString: String): List<Int> {
        val userQueryAst = when (val userQuery = QueryParser.tryParseToEnd(searchString)) {
            is Parsed<FullQuery> -> userQuery.value
            else -> throw IllegalStateException()
        }

        val updatedQuery = searchShorthandEvaluator.handleQuery(userQueryAst)

        val finalQuery = this.databaseQueryCreator.createQueryFrom(updatedQuery)
        val dbQuery = entityManager.createNativeQuery(finalQuery.first)
        finalQuery.second.parameterMap.forEach {
            dbQuery.setParameter(it.first, it.second)
        }

        return (dbQuery.resultStream as Stream<Int>).toList()
    }

    fun search(query: String, sortProperty: String = "name", sortDirection: Sort.Direction = ASC): List<Int> {
        val videoIds = if (query.isNotBlank()) {
            this.query(query)
        } else {
            videoRepository.getAllIds()
        }

        val sorting = Sort.by(sortDirection, sortProperty)
        return if (internalOrderings.contains(sortProperty)) {
            videoRepository.findVideoIdsSortedByOwnProperty(videoIds, sorting)
        } else {
            val metadata = metadataRepository.findByName(sortProperty) ?: throw EntityNotFoundException()
            return when (sortDirection) {
                ASC -> videoRepository.findVideoIdsSortedByAsc(videoIds, metadata.id!!)
                DESC -> videoRepository.findVideoIdsSortedByDesc(videoIds, metadata.id!!)
            }
        }
    }

    companion object {
        private val internalOrderings = setOf("name", "updateTime", "creationTime")
    }
}
