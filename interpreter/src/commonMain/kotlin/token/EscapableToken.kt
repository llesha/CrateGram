package token

import ParserError

abstract class EscapableToken(symbol: String, position: IntRange) : Token(symbol, position) {
    protected open val escapeMap: Map<Char, Char> = mapOf(
        // default escapes
        'b' to '\b',
        'n' to '\n',
        'r' to '\r',
        't' to '\t',
        '\'' to '\'',
        '\"' to '\"',
        '\\' to '\\',
    )

    protected fun convertEscaped():String {
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
        return res.toString()
    }

    protected fun convertEscaped(i: Int): Pair<Char, Int> {
        return if (symbol[i] == 'u') {
            if (i + 4 >= symbol.length)
                throw ParserError("unicode character not ended", (range.first + i)..range.last)
            val number = symbol.substring(i + 1, i + 5).toIntOrNull()
                ?: throw ParserError(
                    "unicode character requires 4-digit hexadecimal",
                    (range.first + i)..(range.first + i + 6)
                )
            Char(number) to 4
        } else {
            (escapeMap[symbol[i]] ?: throw ParserError(
                "escaped character `${symbol[i]}` not found",
                (range.first + i)..(range.first + i + 2)
            )) to 2
        }
    }
}
