import token.*

/**
 * Lexer and parser of PEG grammar
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
    val rules = mutableListOf<RuleWithName>()
    private var index: Int = 0
    private val tokens = mutableListOf<Token>()

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
                val prev = res.last().removeLastOrNull() ?: throw LexerError(
                    "Expected token before =",
                    equalSign.position.first
                )
                res.add(mutableListOf(prev, equalSign))
            }
        }
        res.removeAt(0)
        return res
    }

    private fun tokenizeNext(): Token? {
        skipWhitespace()
        if (index >= text.length)
            return null
        val startIndex = index + 1

        tokenizeSequence(startIndex).ifNotNull { return this }

        return when (text[index]) {
            in tempTokens -> TempToken(text[index].toString(), index..index++)
            '.' -> AnyToken(index..index++)
            '<' -> if (index + 1 < text.length && text[index + 1] == '-') {
                index++
                TempToken("=", index - 1..index++)
            } else throw LexerError("Untokenized input ${text[index]}", index)


            else -> throw LexerError("Untokenized input ${text[index]}", index)
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
        val identRegex = Regex("[a-zA-Z]+")
        if (text[index].toString().matches(identRegex)) {
            val tokenValue = getTextWhileMatches(identRegex)
            return IdentToken(tokenValue, startIndex..index)
        }
        return null
    }

    fun step() {
        while (true) {
            val next = parseNext('#') ?: break
            tokens.add(next)
        }
        var lastRule: RuleWithName? = null
        tokens.forEach {
            if (it is RuleWithName) {
                rules.add(it)
                lastRule = it
            } else {
                lastRule?.rule?.children?.add(it) ?: throw PosError("Expected rule, got $it", index)
            }
        }
        tokens.clear()
    }

    private fun nextChar(): Char {
        index++
        if (index >= text.length)
            throw PosError("Unexpected end of text", index)
        return text[index]
    }

    private fun getTokensUntil(condition: Char): Int {
        val res = tokens.lastIndex
        skipWhitespace()

        var token: Token? = parseNext(condition)
        while (token != null) {
            tokens.add(token)
            token = parseNext(condition)
            // empty block
        }
        skipWhitespace()
        if (text[index] != condition)
            throw PosError("Expected $condition", index)
        index++
        return res
    }

    private fun parseNext(condition: Char): Token? {
        skipWhitespace()
        if (index >= text.length)
            return null
        val startIndex = index + 1

        parseSequences(startIndex).ifNotNull { return this }

        when (text[index]) {
            '|' -> {
                val next = sureParseNext(condition)
                val prev = surePopPrevious()
                if (prev is RuleWithName) {
                    val token = prev.rule.children.removeLast()
                    if (next is Or) {
                        next.children.add(0, token)
                        prev.rule.children.add(next)
                    } else {
                        prev.rule.children.add(Or(token.position.first..next.position.last, mutableListOf(token, next)))
                    }
                    return prev
                }
                if (next is Or) {
                    next.children.add(0, prev)
                    return next
                }
                return Or(prev.position.first..next.position.last, mutableListOf(prev, next))
            }

            '=' -> {
                val next = sureParseNext(condition)
                val prev = surePopPrevious()
                if (next is Or) {
                    next.children.add(0, prev)
                    return next
                }
                if (prev !is IdentToken)
                    throw PosError("Expected identifier before `=` in the rule", prev.position.last)
                val position = prev.position.first..next.position.last
                return RuleWithName(
                    position, prev.symbol,
                    Rule(position, mutableListOf(next))
                )
            }

            // prefix operators
            '&' -> return AndPredicate(startIndex..startIndex, sureParseNext(condition))
            '!' -> return NotPredicate(startIndex..startIndex, sureParseNext(condition))

            // suffix operators
            '*' -> return checkForRuleAndPrefix(Star(index..index++, surePopPrevious()))
            '+' -> return checkForRuleAndPrefix(Plus(index..index++, surePopPrevious()))
            '?' -> return checkForRuleAndPrefix(QuestionMark(index..index++, surePopPrevious()))

            condition -> return null
        }
        throw PosError("Unparsed token ${text[index]}", index)
    }

    private fun parseSequences(startIndex: Int): Token? {
        for ((start, end, tokenClass) in sequenceTokens) {
            if (text[index] == start) {
                index++
                val tokenValue = getTextUntil(end)
                return tokenClass(tokenValue, startIndex until index)
            }
        }
        val identRegex = Regex("[a-zA-Z]+")
        if (text[index].toString().matches(identRegex)) {
            val tokenValue = getTextWhileMatches(identRegex)
            return IdentToken(tokenValue, startIndex..index)
        }
        for ((start, end, tokenClass) in groupTokens) {
            if (text[index] == start) {
                index++
                val idx = getTokensUntil(end)
                if (tokens.lastIndex == idx)
                    throw PosError("Empty group $start$end", index)
                val children = mutableListOf<Token>()
                while (tokens.lastIndex != idx) {
                    children.add(tokens.removeLast())
                }
                return tokenClass(startIndex until index, children.reversed().toMutableList())
            }
        }
        return null
    }

    private fun checkForRuleAndPrefix(suffix: Suffix): Token {
        when (suffix.child) {
            is RuleWithName -> {
                val rule = suffix.child as RuleWithName
                val suffixChild = rule.rule.children.removeLast()
                suffix.child = suffixChild
                // suffix.child might be Prefix, it should be changed by calling method again
                val afterChange = checkForRuleAndPrefix(suffix)
                rule.rule.children.add(afterChange)
                return rule
            }

            is Prefix -> {
                val prefix = suffix.child as Prefix
                val realChild = prefix.child
                suffix.child = realChild
                prefix.child = suffix
                return prefix
            }

            else -> return suffix
        }
    }

    private fun getTextUntil(condition: Char): String {
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
        throw PosError("Out of bounds while tokenizing $condition", index)
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
    }

    private fun getByIndex(): Char {
        if (index >= text.length)
            throw PosError("Out of bounds while lexing a token", index)
        return text[index]
    }

    private fun sureParseNext(condition: Char): Token {
        index++
        return parseNext(condition) ?: throw PosError("Expected valid token after |", index)
    }

    private fun surePopPrevious(): Token {
        return tokens.removeLastOrNull() ?: throw PosError("Expected token before |", index)
    }

    companion object {
        val sequenceTokens = listOf<Triple<Char, Char, (String, IntRange) -> Token>>(
            Triple('"', '"') { value: String, position: IntRange -> LiteralToken(value, position) },
            Triple('\'', '\'') { value: String, position: IntRange -> RegexToken(value, position) }
        )

        private val groupTokens = listOf<Triple<Char, Char, (IntRange, MutableList<Token>) -> Token>>(
            Triple('(', ')') { position: IntRange, children: MutableList<Token> ->
                Group(position, children)
            },
            Triple('[', ']') { position: IntRange, children: MutableList<Token> ->
                Group(position, children)
            })
    }
}
