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
}

abstract class OneChildToken(symbol: String, range: IntRange, var child: Token): Token(symbol, range)

@Deprecated("Regex tokens no longer supported")
class RegexToken(symbol: String, range: IntRange) : Token(symbol, range) {
    private val regex: Regex

    init {
        regex = Regex(symbol)
    }

    override fun toString(): String = "/$symbol/"
}

class LiteralToken(symbol: String, range: IntRange) : Token(symbol, range), Terminal {
    override fun toString(): String = """"$symbol""""
}

/**
 * root - is a starting rule
 */
class IdentToken(symbol: String, range: IntRange) : Token(symbol, range), Terminal

class AnyToken(range: IntRange) : Token(".", range), Terminal

class TempToken(symbol: String, range: IntRange = 0..0) : Token(symbol, range)