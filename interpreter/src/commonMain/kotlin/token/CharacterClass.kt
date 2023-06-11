package token

import InterpreterError
import ParserError
import random

class CharacterClass(symbol: String, position: IntRange = -1..-1) : EscapableToken(symbol, position) {

    override val escapeMap: Map<Char, Char> =
        super.escapeMap + // Character class-specific escapes
                mapOf(
                    '[' to ']',
                    ']' to ']',
                    '-' to '-'
                )

    override fun toString(): String = "[$symbol]"

    private val chars: MutableSet<Char> = mutableSetOf()
    private val ranges = mutableListOf<CharRange>()

    fun getVariants() = chars + rangesToList(ranges)

    init {
        createVariants()
    }

    fun toOr(): Or {
        val allChars = (chars + rangesToList(ranges)).map { Literal(it.toString()) }
        return Or(allChars.toMutableList())
    }

    private fun createVariants() {
        var i = 0
        var prev = '-'
        var escapedPrev = false
        while (i < symbol.length) {
            val next = parseNext(i)
            i += next.third
            if (next.second == '-' && !next.first) {
                parseRange(escapedPrev, i, prev)
                if (i >= symbol.length)
                    break
            } else {
                chars.add(next.second)
            }

            escapedPrev = next.first
            prev = next.second
        }
    }

    private fun parseRange(
        escapedPrev: Boolean,
        i: Int,
        prev: Char
    ): Int {
        if (prev == '-' && !escapedPrev)
            throw InterpreterError("unescaped `-` in range or `-` at the start", range = range)
        val (_, char, iAddition) = parseNext(i)
        ranges.add(prev..char)
        return iAddition
    }

    private fun parseNext(i: Int): Triple<Boolean, Char, Int> {
        if (i >= symbol.length)
            throw InterpreterError("unexpected end of character class", range = range)
        return if (symbol[i] == '\\') {
            val (char, index) = convertEscaped(i + 1)
            Triple(true, char, index)
        } else
            Triple(false, symbol[i], 1)
    }

    fun getRandomVariant(): String {
        val all = chars + ranges
        if (all.isEmpty())
            return ""
        val picked = all.random(random)
        return if (picked is CharRange)
            picked.random(random).toString()
        else (picked as Char).toString()
    }

    private fun rangesToList(ranges: List<CharRange>): List<Char> {
        return ranges
            .map { it.toList() }
            .flatten()
    }
}
