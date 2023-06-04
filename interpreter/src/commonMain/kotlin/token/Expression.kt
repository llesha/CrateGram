package token

import ParserError
import Rules
import listRange
import subList

abstract class Expression(symbol: String, position: IntRange, val children: MutableList<Token>) :
    Token(symbol, position), Container {
    override fun getElements(): List<Token> {
        return children
    }

    override fun replaceElements(rules: Rules, replace: (rules: Rules, token: Token) -> Token) {
        val newChildren = children.toList().map { replace(rules, it) }
        children.clear()
        children.addAll(newChildren)
    }
}

class Group(children: MutableList<Token>, position: IntRange = -1..-1) :
    Expression("(...)", position, children) {

    override fun getRepr(level: Int): String {
        if (level == 0)
            return "e"
        return "(${children.joinToString(separator = " ") { it.getRepr(level - 1) }})"
    }

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

    companion object {
        fun fromList(list: List<Token>): Token {
            if (list.isEmpty())
                throw ParserError("Zero size list")
            if (list.size == 1)
                return list.first()
            if (list.size == 2)
                return Group(mutableListOf(list[0], list[1]), list.listRange())

            val range = list.listRange()
            return Group(mutableListOf(list[0], fromList(list.subList(1).toList())), range)
        }
    }
}

class Or(children: MutableList<Token>, position: IntRange = -1..-1) :
    Expression("|", position, children) {
    override fun getRepr(level: Int): String {
        if (level == 0)
            return "e"
        return children.joinToString(separator = " / ") { it.getRepr(level - 1) }
    }

    constructor(first: Token, second: Token) : this(mutableListOf(first, second))

    override fun toString(): String = children.joinToString(separator = " | ")

    companion object {
        fun fromList(list: List<Token>): Or {
            if (list.size == 2)
                return Or(mutableListOf(list[0], list[1]), list.listRange())
            val range = list.listRange()
            return Or(mutableListOf(list[0], fromList(list.subList(1).toList())), range)
        }
    }
}
