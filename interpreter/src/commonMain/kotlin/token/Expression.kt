package token

abstract class Expression(symbol: String, position: IntRange, val children: MutableList<Token>) :
    Token(symbol, position)

/**
 * Maybe use it as a quantifier for qunatity, like in RegEx
 */
@Deprecated("Use *")
class Repeated(position: IntRange, children: MutableList<Token>) : Expression("{...}", position, children)

class CharacterClass(position: IntRange, children: MutableList<Token>) :
    Expression("[${children.joinToString(separator = "")}]", position, children)

class Group(children: MutableList<Token>, position: IntRange = -1..-1) : Expression("(...)", position, children) {
    override fun toString(): String = "(${children.joinToString(separator = " ")})"
}

class Or(children: MutableList<Token>, position: IntRange = -1..-1) : Expression("|", position, children) {
    override fun toString(): String = children.joinToString(separator = " | ")
}
