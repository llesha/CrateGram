package token

abstract class Prefix(symbol: String, position: IntRange, var child: Token) : Token(symbol, position)

class AndPredicate(position: IntRange, child: Token) : Prefix("&", position, child) {
    override fun toString(): String = "&$child"
}

class NotPredicate(position: IntRange, child: Token) : Prefix("!", position, child) {
    override fun toString(): String = "!$child"
}