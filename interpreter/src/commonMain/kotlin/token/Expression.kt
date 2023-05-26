package token

abstract class Expression(symbol: String, position: IntRange, val children: MutableList<Token>) :
    Token(symbol, position)

class Group(children: MutableList<Token>, position: IntRange = -1..-1) :
    Expression("(...)", position, children) {
    constructor(first: Token, second: Token) : this(mutableListOf(first, second))

    override fun toString(): String = toStringMindingParentheses()

    fun toStringMindingParentheses(isInsideGroup: Boolean = false): String {
        val childrenRepresentation = children.joinToString(separator = " ") {
            when (it) {
                is Or -> "($it)"
                is Group -> it.toStringMindingParentheses(true)
                else -> it.toString()
            }
        }
        return if (isInsideGroup) childrenRepresentation else "($childrenRepresentation)"
    }
}

class Or(children: MutableList<Token>, position: IntRange = -1..-1) :
    Expression("|", position, children) {
    constructor(first: Token, second: Token) : this(mutableListOf(first, second))

    override fun toString(): String = children.joinToString(separator = " | ")
}
