package token

class Literal(symbol: String, range: IntRange = -1..-1) : EscapableToken(symbol, range) {
    val withoutEscapes = run {
        val res = StringBuilder()
        var i = 0
        while(i < symbol.length) {
            if(symbol[i] == '\\') {
                val(char, newI) = convertEscaped(i + 1)
                i += newI
                res.append(char)
            } else {
                res.append(symbol[i])
                i++
            }
        }
        res.toString()
    }
    override fun getRepr(level: Int): String {
        if (level == 0)
            return "e"
        return if (symbol == "") "ε" else "a"
    }

    override fun toString(): String = """"$symbol""""
}