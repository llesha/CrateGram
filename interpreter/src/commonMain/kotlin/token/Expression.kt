package token

import ParserError
import listRange
import subList

abstract class Expression(symbol: String, position: IntRange, val children: MutableList<Token>) :
    Token(symbol, position)

class Group(first: Token, second: Token, position: IntRange = -1..-1) :
    Expression("(...)", position, mutableListOf(first, second)) {
    override fun toString(): String = "(${children.joinToString(separator = " ")})"

    companion object {
        fun fromList(list: List<Token>): Token {
            if (list.isEmpty())
                throw ParserError("Zero size list")
            if (list.size == 1)
                return list.first()
            if (list.size == 2)
                return Group(list[0], list[1], list.listRange())
            val range = list.listRange()
            return Group(list[0], fromList(list.subList(1).toList()), range)
        }
    }
}

class Or(first: Token, second: Token, position: IntRange = -1..-1) :
    Expression("|", position, mutableListOf(first, second)) {
    override fun toString(): String = children.joinToString(separator = " | ")

    companion object {
        fun fromList(list: List<Token>): Or {
            if (list.size == 2)
                return Or(list[0], list[1], list.listRange())
            val range = list.listRange()
            return Or(list[0], fromList(list.subList(1).toList()), range)
        }
    }
}
