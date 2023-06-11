package token

import ParserError

abstract class Token(val symbol: String, var range: IntRange) {
    fun copy(): Token {
        return when (this) {
            is IdentToken -> IdentToken(symbol, range)
            is Literal -> Literal(symbol, range)
            is AnyToken -> AnyToken(range)
            is NotPredicate -> NotPredicate(child.copy())
            is Group -> children[0].copy() and children[1].copy()
            is Or -> children[0].copy() / children[1].copy()
            else -> throw Exception("Cannot clone $this")
        }
    }

    open fun getRepr(level: Int = 1): String {
        throw ParserError("getRepr shouldn't be called for $symbol")
    }

    override fun toString(): String = symbol

    override fun hashCode(): Int {
        return symbol.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        return symbol == (other as Token).symbol
    }

    fun toRule(): Rule = Rule(this)

    operator fun div(other: Token): Or {
        return Or(mutableListOf(this, other))
    }

    operator fun not(): NotPredicate {
        return NotPredicate(this)
    }

    infix fun and(other: Token): Group {
        return Group(this, other)
    }

    companion object {
        /** @return ε */
        fun empty() = Literal("")

        /** @return F */
        fun fail() = IdentToken("\$F")

        /** @return Z */
        fun any() = IdentToken("\$Z")

        /** @return T */
        fun anyTerminal() = IdentToken("\$T")

        fun emptyRule() = IdentToken("\$E")
    }
}

abstract class OneChildToken(symbol: String, range: IntRange, var child: Token) : Token(symbol, range)

/**
 * root - is a starting rule
 */
open class IdentToken(symbol: String, range: IntRange = -1..-1) : Token(symbol, range) {
    override fun getRepr(level: Int): String {
        if (level == 0)
            return "e"
        return "N"
    }

    /**
     * To convert GeneratedToken to IdentToken
     */
    fun toIdent(): IdentToken {
        return IdentToken(symbol, range)
    }
}

class AnyToken(range: IntRange = -1..-1) : Token(".", range) {
    override fun getRepr(level: Int): String {
        return "a"
    }
}

class TempToken(symbol: String, range: IntRange = -1..-1) : Token(symbol, range)

class GeneratedToken(symbol: String, val inPlaceOfPrevious: String) : IdentToken(symbol)