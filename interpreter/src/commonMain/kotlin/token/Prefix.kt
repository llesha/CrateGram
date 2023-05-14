package token

abstract class Prefix(symbol: String, position: IntRange, child: Token) : OneChildToken(symbol, position, child)

class AndPredicate(position: IntRange, child: Token) : Prefix("&", position, child) {
    override fun toString(): String = "&$child"
}

class NotPredicate(position: IntRange, child: Token) : Prefix("!", position, child) {
    override fun toString(): String = "!$child"
}