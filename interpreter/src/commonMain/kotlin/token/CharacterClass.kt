package token

import InterpreterError

class CharacterClass(symbol: String, position: IntRange = -1..-1) : Token(symbol, position) {
    override fun toString(): String = "[$symbol]"

    val variants: CharacterClassVariant

    init {
        variants = createVariants()
    }

    /**
     * Currently [CharacterClassVariant.normal] contain characters from [CharacterClassVariant.ranges].
     */
    private fun createVariants(): CharacterClassVariant {
        var i = 0
        val escaped = mutableSetOf<Char>()
        val normal = mutableSetOf<Char>()
        val ranges = mutableListOf<VariantRange>()
        var prev = '-'
        var escapedPrev = false
        while (i < symbol.length) {
            val next = parseNext(i)
            i += next.third
            if (next.second == '-' && !next.first) {
                parseRange(escapedPrev, i, ranges, prev)
                if (i >= symbol.length)
                    break
            } else {
                addToCollection(next.first, next.second, normal, escaped)
            }

            escapedPrev = next.first
            prev = next.second
        }
        return CharacterClassVariant(normal, escaped, ranges)
    }

    private fun parseRange(
        escapedPrev: Boolean,
        i: Int,
        ranges: MutableList<VariantRange>,
        prev: Char
    ): Int {
        if (prev == '-' && !escapedPrev)
            throw InterpreterError("unescaped `-` in range or `-` at the start", range = range)
        val (isEscaped, char, iAddition) = parseNext(i)
        ranges.add(VariantRange(isTrulyEscaped(prev, escapedPrev), isTrulyEscaped(char, isEscaped), prev..char))
        return iAddition
    }

    private fun addToCollection(
        isEscaped: Boolean,
        char: Char,
        normal: MutableCollection<Char>,
        escaped: MutableCollection<Char>
    ) {
        if (isTrulyEscaped(char, isEscaped))
            escaped.add(char)
        else
            normal.add(char)
    }

    private fun isTrulyEscaped(char: Char, supposedlyEscaped: Boolean): Boolean =
        supposedlyEscaped && char !in specificEscapes

    private fun parseNext(i: Int): Triple<Boolean, Char, Int> {
        if (i >= symbol.length)
            throw InterpreterError("unexpected end of character class", range = range)
        return if (symbol[i] == '\\')
            Triple(true, symbol[i + 1], 2)
        else
            Triple(false, symbol[i], 1)
    }

    companion object {
        private const val specificEscapes = "[]-"
    }

    data class CharacterClassVariant(
        val normal: Set<Char>,
        val escaped: Set<Char>,
        val ranges: List<VariantRange>
    )

    data class VariantRange(val firstEscaped: Boolean, val secondEscaped: Boolean, var range: CharRange)
}