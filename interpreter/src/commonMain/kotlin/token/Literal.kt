package token

class Literal(symbol: String, range: IntRange = -1..-1) : EscapableToken(symbol, range) {
    val withoutEscapes = convertEscaped()
    override fun getRepr(level: Int): String {
        if (level == 0)
            return "e"
        return if (symbol == "") "ε" else "a"
    }

    override fun toString(): String = """"$symbol""""
}