package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parsed
import org.junit.Test

class QueryParserTest {
    @Test
    fun itParsesSimpleQueries() {
        assert(QueryParser.tryParseToEnd("simpleproperty") is Parsed)
        assert(QueryParser.tryParseToEnd("some values") is Parsed)
        assert(QueryParser.tryParseToEnd("123") is Parsed)
    }

    @Test
    fun itParsesExactText() {
        assert(QueryParser.tryParseToEnd("\"123\"") is Parsed)
        assert(QueryParser.tryParseToEnd("text:\"123\"") is Parsed)
        assert(QueryParser.tryParseToEnd("text:\"123 with spaces\"") is Parsed)
    }

    @Test
    fun itParsesPropertyChecks() {
        assert(QueryParser.tryParseToEnd("test:123") is Parsed)
        assert(QueryParser.tryParseToEnd("test:Hello") is Parsed)
        assert(QueryParser.tryParseToEnd("text:\"123 with spaces\"") is Parsed)
        assert(QueryParser.tryParseToEnd("text:\"123 with spaces\" and some others") is Parsed)
    }

    @Test
    fun itParsesBracketExpressions() {
        assert(QueryParser.tryParseToEnd("(test:123)") is Parsed)
        assert(QueryParser.tryParseToEnd("(test)") is Parsed)
        assert(QueryParser.tryParseToEnd("(((((((test)))))))") is Parsed)
        assert(QueryParser.tryParseToEnd("(abc) (abd)") is Parsed)
    }

    @Test
    fun itParsesCombinationExpressions() {
        assert(QueryParser.tryParseToEnd("test:123 AND test") is Parsed)
        assert(QueryParser.tryParseToEnd("(test) OR (test)") is Parsed)
        assert(QueryParser.tryParseToEnd("(((((((test))))) AND (test)))") is Parsed)
        assert(QueryParser.tryParseToEnd("A OR B") is Parsed)
    }

    @Test
    fun itParsesComparisonChecks() {
        assert(QueryParser.tryParseToEnd("test<123") is Parsed)
        assert(QueryParser.tryParseToEnd("test>Hello") is Parsed)
        assert(QueryParser.tryParseToEnd("text<\"123 with spaces\"") is Parsed)
        assert(QueryParser.tryParseToEnd("text<\"123 with spaces\" and some others") is Parsed)
    }
}
