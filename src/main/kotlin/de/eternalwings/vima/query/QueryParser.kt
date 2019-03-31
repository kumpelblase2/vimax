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
import com.github.h0tk3y.betterParse.parser.Parser

object QueryParser : Grammar<FullQuery>() {

    // Base tokens
    val plus by token("\\+")
    val minus by token("-")
    val colon by token(":")
    val smaller by token("<")
    val larger by token(">")
    val AND by token("AND|and")
    val OR by token("OR|or")
    val openBrace by token("\\(")
    val closeBrace by token("\\)")
    val word by token("\\w+")
    val ws by token("\\s+")
    val quoteString by token("\"[^\"]*\"")

    val value by (quoteString.map { it.text.substring(0, it.text.length - 1) } or word.map { it.text })

    val boolean by plus.map { BooleanOp(true) } or minus.map { BooleanOp(false) }
    val comparator by smaller.map { Comparator.SMALLER } or larger.map { Comparator.LARGER }

    val property by (word and skip(colon) and value).map { PropertyQuery(it.t1.text, it.t2) }
    val comparison by (word and comparator and value).map { ComparisonQuery(it.t1.text, it.t2, it.t3) }
    val text by value.map { TextQuery(it) }

    val term: Parser<QueryPart> by (optional(boolean) and (property or comparison or text)).map {
        val boolOp = it.t1
        if (boolOp != null) {
            BooleanQuery(it.t2, boolOp.value)
        } else {
            it.t2
        }
    }

    val orExpr: Parser<QueryPart> by (parser(::andExpr) and -ws and OR and -ws and parser(
            ::orExpr)).map { (first, _, second) ->
        when {
            first is UnionQuery -> UnionQuery(first.parts + second)
            second is UnionQuery -> UnionQuery(second.parts + first)
            else -> UnionQuery(listOf(first, second))
        }
    } or parser(::andExpr)

    val andExpr: Parser<QueryPart> by (parser(::expr) and -ws and AND and -ws and parser(
            ::andExpr)).map { (first, _, second) ->
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

