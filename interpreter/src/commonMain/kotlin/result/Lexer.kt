package result

import LexerError
import ifNotNull
import listRange
import token.*

/**
 * result.Lexer and parser of PEG grammar
 *
 *
 * ## Operator Type Precedence Description
 * * " "  5 Literal string
 * * [ ] 5 Character class
 * * . 5 Any character
 * * (e) 5 Grouping
 * * e? 4 Optional
 * * e*  4 Zero-or-more
 * * e+  4 One-or-more
 * * &e  3 And-predicate
 * * !e  3 Not-predicate
 * * e1 e2 2 Sequence
 * e1 | e2 - 1 Prioritized Choice
 *
 */
class Lexer(private val text: String) {
    private var index: Int = 0

    private val tempTokens = mutableSetOf(
        '(', ')',
        '!', '&',
        '*', '+', '?',
        '|', '/', '='
    )

    fun tokenize(): List<List<Token>> {
        val res = mutableListOf<MutableList<Token>>(mutableListOf())
        while (true) {
            res.last().add(tokenizeNext() ?: break)
            if (res.last().last().symbol == "=") {
                val equalSign = res.last().removeLast()
                val prev = res.last().removeLastOrNull() ?: throw LexerError("expected token before =", equalSign.range)
                res.add(mutableListOf(prev, equalSign))
            }
        }
        if(res[0].isNotEmpty()) {
            throw LexerError("Extra elements", res[0].listRange())
        }
        res.removeAt(0)
        return res
    }

    private fun tokenizeNext(): Token? {
        skipWhitespace()
        if (index >= text.length)
            return null
        val startIndex = index

        tokenizeSequence(startIndex).ifNotNull { return this }

        return when (text[index]) {
            in tempTokens -> TempToken(text[index].toString(), index..index++)
            '.' -> AnyToken(index..index++)
            '<' -> if (index + 1 < text.length && text[index + 1] == '-') {
                index++
                TempToken("=", index - 1..index++)
            } else throw LexerError("untokenized input ${text[index]}", position = index)

            else -> throw LexerError("untokenized input ${text[index]}", position = index)
        }
    }

    private fun tokenizeSequence(startIndex: Int): Token? {
        for ((start, end, tokenClass) in sequenceTokens) {
            if (text[index] == start) {
                index++
                val tokenValue = getTextUntil(end)
                return tokenClass(tokenValue, startIndex until index)
            }
        }
        val identRegex = Regex("[a-zA-Z_]\\w*")
        if (text[index].toString().matches(identRegex)) {
            val tokenValue = getTextWhileMatches(identRegex)
            return IdentToken(tokenValue, startIndex..index)
        }
        return null
    }

    private fun getTextUntil(condition: Char): String {
        val startIndex = index - 1
        val res = StringBuilder()
        while (index < text.length) {
            if (text[index] == condition) {
                index++
                return res.toString()
            }
            if (text[index] == '\\') {
                res.append(text[index++])
                res.append(getByIndex())
            } else {
                res.append(text[index])
            }
            index++
        }
        throw LexerError("out of bounds while tokenizing $condition", startIndex..index)
    }

    private fun getTextWhileMatches(regex: Regex): String {
        val res = StringBuilder()
        res.append(text[index])
        while (res.matches(regex)) {
            if (index + 1 >= text.length) {
                index++
                return res.toString()
            }
            res.append(nextChar())
        }
        return res.deleteAt(res.lastIndex).toString()
    }

    private fun skipWhitespace() {
        while (index < text.length && text[index].isWhitespace())
            index++
        // skip multiline comment
        if (index < text.length - 1 && text[index] == '(' && text[index + 1] == '*') {
            val startIndex = index
            index += 2
            while (index < text.length - 1 && (text[index] != '*' || text[index + 1] != ')'))
                index++
            if (index == text.lastIndex || (text[index] != '*' && text[index + 1] != ')'))
                throw LexerError("unclosed multiline comment", startIndex..startIndex + 1)
            index += 2
            skipWhitespace()
        }
        // skip line comment
        if (index < text.length && text[index] == '#') {
            while (index < text.length && text[index] != '\n')
                index++
            skipWhitespace()
        }
    }

    private fun getByIndex(): Char {
        if (index >= text.length)
            throw LexerError("out of bounds while lexing a token", position = index)
        return text[index]
    }

    private fun nextChar(): Char {
        index++
        if (index >= text.length)
            throw LexerError("unexpected end of text", position = index)
        return text[index]
    }

    companion object {
        val sequenceTokens = listOf<Triple<Char, Char, (String, IntRange) -> Token>>(
            Triple('"', '"') { value: String, position: IntRange -> Literal(value, position) },
            Triple('\'', '\'') { value: String, position: IntRange -> Literal(value, position) },
            Triple('[', ']') { value: String, position: IntRange ->
                    CharacterClass(value, position)
            },
            Triple('{', '}') { value: String, position: IntRange ->
                Repeated(position, Literal(value))
            }
        )

        private val groupTokens = listOf<Triple<Char, Char, (IntRange, MutableList<Token>) -> Token>>(
            Triple('(', ')') { position: IntRange, children: MutableList<Token> ->
                Group(children, position)
            },
        )
    }
}
