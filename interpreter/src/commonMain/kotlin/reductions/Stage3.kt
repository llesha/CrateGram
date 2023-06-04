package reductions

import Rules
import token.*

/**
 * Removes [NotPredicate] from rules
 */
fun stage3(rules: Rules) {
    val ruleTokens = rules.values.toMutableList()
    for (i in ruleTokens.indices) {
        val tokenRule = ruleTokens[i].child
        ruleTokens[i].child = rules.h1(tokenRule)
    }
}

/**
 * 1. d(A,e) = e, if e ∈ {ε,F}. (for any tokens)
 * 2. d(A,e1e2) = d(A,e1) d(A,e2).
 * 3. d(A,e1/e2) = d(A,e1) / d(A,e2).
 * 4. d(A,!e) =!(A e).
 */
fun Rules.d(A: IdentToken, e: Token): Token {
    return when (e.getRepr(1)) {
        "(e e)" -> d(A, (e as Group).children[0]) and d(A, e.children[1])
        "e / e" -> d(A, (e as Or).children[0]) / d(A, e.children[1])
        "!e" -> !(A and (e as NotPredicate).child)
        else -> e
    }
}

/**
 * n(e,C) = (e (Z / ε) / ε) C
 */
fun Rules.n(e: Token, C: IdentToken): Token {
    return ((e and (Token.any() / Token.empty())) / Token.empty()) and C
}

fun Rules.h0(e: Token, C: IdentToken): Token {
    when (e.getRepr(1)) {
        "ε" -> return C
        "a" -> if (e == Token.fail()) return e
        "(e e)" -> {
            val (e1, e2) = (e as Group).children
            return n(n(h0(e1, C), C) / n(h0(e2, C), C), C)
        }

        "e / e" -> {
            val (e1, e2) = (e as Or).children
            return h0(e1, C) / h0(e2, C)
        }

        "!e" -> {
            if ((e as NotPredicate).child is Or) {
                val or = e.child as Or
                if (or.children[0] is IdentToken)
                    return n(or.children[0] / h0(or.children[1], C), C)
            }
            if (e.child is Group) {
                val group = e.child as Group
                if (group.children[0] is IdentToken
                    && group.children[1] is Or
                    && (group.children[1] as Or).children[0] is IdentToken
                )
                    return n(
                        group.children[1] and
                                ((group.children[1] as Or).children[0] / h0((group.children[1] as Or).children[1], C)),
                        C
                    )
            }
        }
    }
    if (e is Expression) {
        val newChildren = e.children.toList().map { h0(it, C) }
        e.children.clear()
        e.children.addAll(newChildren)
    } else if (e is NotPredicate) {
        e.child = h0(e.child, C)
    }
    return e
}

fun Rules.h1(e: Token): Token {
    when (e.getRepr(1)) {
        "(e e)" -> {
            e as Group
            if (e.children[0] is IdentToken && e.children[1] is IdentToken)
                return e
            if (e.children[1] is IdentToken)
                return h0(e.children[0], e.children[1] as IdentToken)
            if (e.children[0] is IdentToken)
                return h0(d(e.children[0] as IdentToken, e.children[1]), e.children[0] as IdentToken)
        }

        "e / e" -> return h1((e as Or).children[0]) / h1(e.children[1])
    }
    if (e is Container) {
        e.replaceElements(this) { rules: Rules, token: Token -> h1(token) }
    }
    return e
}