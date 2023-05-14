package token

import LexerError

abstract class Suffix(symbol: String, position: IntRange, child: Token) : OneChildToken(symbol, position, child)

class Star(position: IntRange, child: Token) : Suffix("*", position, child) {
    override fun toString(): String = "$child*"
}

class Plus(position: IntRange, child: Token) : Suffix("+", position, child) {
    override fun toString(): String = "$child+"
}

class QuestionMark(position: IntRange, child: Token) : Suffix("?", position, child) {
    override fun toString(): String = "$child?"
}

/**
 * Use it as a quantifier for quantity, like in RegEx
 */
class Repeated(position: IntRange, child: Token) : Suffix("{}", position, child) {
    val quantity: Int

    init {
        val num = child.symbol.toIntOrNull()
        if (num == null || num <= 0)
            throw LexerError("{} expression should contain positive number", position)
        quantity = num
    }

    override fun toString(): String = "$child{$quantity}"
}
