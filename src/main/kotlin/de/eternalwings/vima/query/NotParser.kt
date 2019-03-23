package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.NoMatchingToken
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser

class NotParser<S>(val parser: Parser<S>) : Parser<TokenMatch> {
    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<TokenMatch> {
        val result = parser.tryParse(tokens)
        return when (result) {
            is ErrorResult -> Parsed(tokens.first(), tokens.drop(1))
            else -> NoMatchingToken(tokens.first())
        }
    }
}

fun <S> not(parser: Parser<S>) = NotParser(parser)
