package token

abstract class Token(val symbol: String, var position: IntRange) {
    override fun toString(): String = symbol
}

@Deprecated("Regex tokens no longer supported")
class RegexToken(symbol: String, position: IntRange) : Token(symbol, position) {
    private val regex: Regex

    init {
        regex = Regex(symbol)
    }

    override fun toString(): String = "/$symbol/"
}

class LiteralToken(symbol: String, position: IntRange) : Token(symbol, position), Terminal {
    override fun toString(): String = """"$symbol""""
}

/**
 * root - is a starting rule
 */
class IdentToken(symbol: String, position: IntRange) : Token(symbol, position), Terminal

class AnyToken(position: IntRange) : Token(".", position), Terminal

class TempToken(symbol: String, position: IntRange) : Token(symbol, position)