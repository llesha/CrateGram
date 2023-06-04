package token

import Rules

abstract class Prefix(symbol: String, position: IntRange, child: Token) : OneChildToken(symbol, position, child),
    Container {
    override fun getElements(): List<Token> {
        return listOf(child)
    }

    override fun replaceElements(rules: Rules, replace: (rules: Rules, token: Token) -> Token) {
        val newChild = replace(rules, child)
        child = newChild
    }
}

class AndPredicate(position: IntRange, child: Token) : Prefix("&", position, child) {
    override fun toString(): String = "&$child"
}

class NotPredicate(child: Token, position: IntRange = -1..-1) : Prefix("!", position, child) {
    override fun getRepr(level: Int): String {
        if (level == 0)
            return "e"
        return "!${child.getRepr(level - 1)}"
    }

    override fun toString(): String = "!${if (child is Or) "($child)" else child}"
}