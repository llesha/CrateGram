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

    companion object {
        fun empty() = Literal("")
    }
}

abstract class OneChildToken(symbol: String, range: IntRange, var child: Token): Token(symbol, range)

class Literal(symbol: String, range: IntRange = -1..-1) : Token(symbol, range), Terminal {
    override fun toString(): String = """"$symbol""""
}

/**
 * root - is a starting rule
 */
class IdentToken(symbol: String, range: IntRange = -1..-1) : Token(symbol, range), Terminal

class AnyToken(range: IntRange) : Token(".", range), Terminal

class TempToken(symbol: String, range: IntRange = -1..-1) : Token(symbol, range)