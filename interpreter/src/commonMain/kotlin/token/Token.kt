package token

abstract class Token(val symbol: String, var range: IntRange) {
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

    companion object {
        /**
         * @return ε
         */
        fun empty() = Literal("")

        /**
         * @return F
         */
        fun fail() = IdentToken("\$F")

        /**
         * @return Z
         */
        fun any() = IdentToken("\$Z")

        /**
         * @return T
         */
        fun anyTerminal() = IdentToken("\$T")

        fun emptyRule() = IdentToken("\$E")

    }

    operator fun div(other: Token): Or {
        return Or(mutableListOf(this, other))
    }

    operator fun not(): NotPredicate {
        return NotPredicate(this)
    }

    infix fun and(other: Token): Group {
        return Group(this, other)
    }
}

abstract class OneChildToken(symbol: String, range: IntRange, var child: Token) : Token(symbol, range)

class Literal(symbol: String, range: IntRange = -1..-1) : Token(symbol, range), Terminal {
    override fun toString(): String = """"$symbol""""
}

/**
 * root - is a starting rule
 */
open class IdentToken(symbol: String, range: IntRange = -1..-1) : Token(symbol, range), Terminal {
    /**
     * To convert GeneratedToken to IdentToken
     */
    fun toIdent(): IdentToken {
        return IdentToken(symbol, range)
    }
}

class AnyToken(range: IntRange = -1..-1) : Token(".", range), Terminal

class TempToken(symbol: String, range: IntRange = -1..-1) : Token(symbol, range)

class GeneratedToken(symbol: String, val inPlaceOfPrevious: String) : IdentToken(symbol)