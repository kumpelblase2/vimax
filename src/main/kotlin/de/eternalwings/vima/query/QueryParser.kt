package de.eternalwings.vima.query

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separated
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object QueryParser : Grammar<FullQuery>() {

    private fun String.unquote(): String {
        return if (this.startsWith("\"") && this.endsWith("\"")) {
            this.substring(1, this.length - 1)
        } else {
            this
        }
    }

    // Base tokens
    val plus by regexToken("\\+")
    val minus by literalToken("-")
    val colon by literalToken(":")
    val smaller by literalToken("<")
    val larger by literalToken(">")
    val equals by literalToken("=")
    val like by literalToken("~")
    val AND by regexToken("AND|and")
    val OR by regexToken("OR|or")
    val openBrace by regexToken("\\(")
    val closeBrace by regexToken("\\)")
    val word by regexToken("\\w+")
    val ws by regexToken("\\s+")
    val quoteString by regexToken("\"[^\"]*\"")

    val value by (quoteString.map { it.text.unquote() } or word.map { it.text })

    val boolean by plus.map { BooleanOp(true) } or minus.map { BooleanOp(false) }
    val comparator by ((smaller.map { Comparator.SMALLER } or larger.map { Comparator.GREATER }) and
            optional(equals)).map {
        if (it.t2 != null) {
            when (it.t1) {
                Comparator.SMALLER -> Comparator.SMALLER_OR_EQUALS
                Comparator.GREATER -> Comparator.GREATER_OR_EQUALS
                else -> throw IllegalStateException()
            }
        } else {
            it.t1
        }
    }

    val property by (value and skip(colon) and value).map { PropertyQuery(it.t1, it.t2) }
    val likeProperty by (value and skip(like) and value).map { PropertyQuery(it.t1, it.t2, true) }
    val comparison by (value and comparator and value).map { ComparisonQuery(it.t1, it.t2, it.t3) }
    val text by value.map { TextQuery(it) }

    val term: Parser<QueryPart> by (optional(boolean) and (property or likeProperty or comparison or text)).map {
        val boolOp = it.t1
        if (boolOp != null) {
            BooleanQuery(it.t2, boolOp.value)
        } else {
            it.t2
        }
    }

    val orExpr: Parser<QueryPart> by (parser(::andExpr) and -ws and OR and -ws and parser(::orExpr))
        .map { (first, _, second) ->
            when {
                first is UnionQuery -> UnionQuery(first.parts + second)
                second is UnionQuery -> UnionQuery(second.parts + first)
                else -> UnionQuery(listOf(first, second))
            }
        } or parser(::andExpr)

    val andExpr: Parser<QueryPart> by (parser(::expr) and -ws and AND and -ws and parser(::andExpr))
        .map { (first, _, second) ->
        when {
            first is IntersectionQuery -> IntersectionQuery(first.parts + second)
            second is IntersectionQuery -> IntersectionQuery(second.parts + first)
            else -> IntersectionQuery(listOf(first, second))
        }
    } or parser(::expr)

    val expr: Parser<QueryPart> by (skip(openBrace) and separated(orExpr, ws) and skip(closeBrace)).map {
        if (it.terms.size == 1) {
            it.terms.first()
        } else {
            UnionQuery(it.terms)
        }
    } or term

    val query by separated(orExpr, ws).use {
        reduce { a, _, b ->
            if (a is IntersectionQuery) {
                IntersectionQuery(a.parts + b)
            } else {
                IntersectionQuery(listOf(a, b))
            }
        }
    }

    override val rootParser: Parser<FullQuery>
        get() = query.map { FullQuery(it) }

}

