package token

import InterpreterError
import ParserError
import random

class CharacterClass(symbol: String, position: IntRange = -1..-1) : Token(symbol, position) {
    override fun toString(): String = "[$symbol]"

    private val escapeMap: Map<Char, Char> = mutableMapOf(
        // default escapes
        't' to '\t',
        'b' to '\b',
        'n' to '\n',
        'r' to '\r',
//        "'" to '\'',
        '\"' to '\"',
        '\\' to '\\',
        // PEG-specific escapes
        '[' to ']',
        ']' to ']',
        '-' to '-',
    )

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
            convertEscaped(i + 1)
        } else
            Triple(false, symbol[i], 1)
    }

    private fun convertEscaped(i: Int): Triple<Boolean, Char, Int> {
        return if (symbol[i] == 'u') {
            if (i + 4 >= symbol.length)
                throw ParserError("")
            val number = symbol.substring(i + 1, i + 5).toIntOrNull() ?: throw ParserError("")
            Triple(true, Char(number), 4)
        } else {
            Triple(true, escapeMap[symbol[i]] ?: throw ParserError(""), 2)
        }
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
