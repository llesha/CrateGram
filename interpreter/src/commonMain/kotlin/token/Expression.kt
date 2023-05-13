package token

abstract class Expression(symbol: String, position: IntRange, val children: MutableList<Token>) : Token(symbol, position)

@Deprecated("Use *")
class Repeated(position: IntRange, children: MutableList<Token>) : Expression("{...}", position, children)

@Deprecated("Use ?")
class Optional(position: IntRange, children: MutableList<Token>) : Expression("[...]", position, children)

class Group(position: IntRange, children: MutableList<Token>) : Expression("(...)", position, children) {
    override fun toString(): String = "(${children.joinToString(separator = " ")})"
}

class Or(position: IntRange, children: MutableList<Token>): Expression("|", position, children) {
    override fun toString(): String = children.joinToString(separator = " | ")
}
