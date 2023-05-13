package token

abstract class Suffix(symbol: String, position: IntRange, var child: Token) : Token(symbol, position)

class Star(position: IntRange, child: Token) : Suffix("*", position, child) {
    override fun toString(): String = "$child*"
}

class Plus(position: IntRange, child: Token) : Suffix("+", position, child) {
    override fun toString(): String = "$child+"
}

class QuestionMark(position: IntRange, child: Token) : Suffix("?", position, child) {
    override fun toString(): String = "$child?"
}