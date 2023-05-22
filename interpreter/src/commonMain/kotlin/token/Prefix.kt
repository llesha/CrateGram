package token

abstract class Prefix(symbol: String, position: IntRange, child: Token) : OneChildToken(symbol, position, child)

class AndPredicate(position: IntRange, child: Token) : Prefix("&", position, child) {
    override fun toString(): String = "&$child"
}

class NotPredicate(child: Token, position: IntRange = -1..-1) : Prefix("!", position, child) {
    override fun toString(): String = "!$child"
}