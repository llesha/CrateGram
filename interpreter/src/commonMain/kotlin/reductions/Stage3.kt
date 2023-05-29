package reductions

import Rules
import assert
import token.*

/**
 * Removes [NotPredicate] from rules
 */
fun stage3(rules: Rules) {
    val ruleTokens = rules.values.toMutableList()
    for (i in ruleTokens.indices) {
        val tokenRule = ruleTokens[i].child
        ruleTokens[i].child = h1(tokenRule)
    }
}

fun d(A: IdentToken, e: Token): Token {
    when (e) {
        is Group -> return d(A, e.children[0]) and d(A, e.children[1])
        is Or -> return d(A, e.children[0]) / d(A, e.children[1])
        is NotPredicate -> return !(A and e)
        else -> {
            if (e is IdentToken) {
                assert(e == Token.fail())
                return e
            }
            if (e is Literal) {
                assert(e == Token.empty())
                return e
            }
            throw Exception("")
        }
    }
}

/**
 * n(e,C) = (e (Z / ε) / ε) C
 */
fun n(e: Token, C: IdentToken): Token {
    return ((e and (Token.any() / Token.empty())) / Token.empty()) and C
}

fun h0(e: Token, C: IdentToken): Token {
    when (e) {
        is Literal -> {
            assert(e.symbol == "")
            return C
        }

        is IdentToken -> {
            assert(e == Token.fail())
            return e
        }

        is Group -> {
            val (e1, e2) = e.children
            return n(n(h0(e1, C), C) / n(h0(e2, C), C), C)
        }

        is Or -> return Or(e.children.map { h0(it, C) }.toMutableList())

        is NotPredicate -> {
            if (e.child is Or) {
                val or = e.child as Or
                assert(or.children[0] is IdentToken)
                return n(or.children[0] / h0(or.children[1], C), C)
            }
            if (e.child is Group) {
                val group = e.child as Group
                assert(group.children[0] is IdentToken)
                assert(group.children[1] is Or)
                return n(
                    group.children[1] and
                            ((group.children[1] as Or).children[0] / h0((group.children[1] as Or).children[1], C)),
                    C
                )
            } else throw Exception("Expected Or or Group ${e.child}")
        }

        else -> throw Exception("Unexpected token")
    }
}

fun h1(e: Token): Token {
    when (e) {
        is Group -> {
            if (e.children[0] is IdentToken && e.children[1] is IdentToken)
                return e
            if (e.children[1] is IdentToken)
                return h0(e.children[0], e.children[1] as IdentToken)
            if (e.children[0] is IdentToken)
                return h0(d(e.children[0] as IdentToken, e.children[1]), e.children[0] as IdentToken)
            throw Exception("Expected at least one Ident token in group $e")
        }

        is Or -> return Or(e.children.map { h1(it) }.toMutableList())
        // a, A
        else -> return e
    }
}