package reductions

import Rules
import token.*

/**
 * Split grammar into [g0] and [g1]
 */
fun stage2(rules: Rules) {
    val ruleTokens = rules.keys.filter { it.symbol != "root" }.map { rules[it]!! }.toMutableList()
    for (i in ruleTokens.indices) {
        val tokenRule = ruleTokens[i].child
        ruleTokens[i].child = rules.h1(rules.g1(tokenRule))
    }
    val rootRule = rules[IdentToken("root")]!!.child
    println(rootRule)
    rules[IdentToken("root")] =
        (rules.h1(rules.g1(rootRule.copy())) / rules.h0(rules.g0(rootRule.copy()), Token.anyTerminal())).toRule()
    println(rootRule)
}

private fun Rules.g0WithStatus(e: Token): Pair<Token, Boolean> {
    when (e.getRepr(1)) {
        // g0(ε) = ε
        "ε" -> return e to true
        // g0(a) = F
        "a" -> return Token.fail() to false
        // g0(A) = R_G(A)  (to expression that is defined by rule A)
        "N" -> {
            if (e.symbol in listOf("\$F", "\$Z", "\$T"))
                return e to false
            if(e.symbol == "\$E")
                return e to true
            return g0WithStatus(getRule(e))
        }
        //
        "(e e)" -> {
            if (e.getRepr(2) == "NN") {
                e as Group
                val first = g0WithStatus(e.children[0])
                if (first.second)
                    return (first.first and g0WithStatus(e.children[1]).first) to true
                return Token.fail() to false
            }
        }
        // g0(e1 / e2) = g0(e1) / g0(e2)
        "e / e" -> {
            e as Or
            val (first, status0) = g0WithStatus(e.children[0])
            val (second, status1) = g0WithStatus(e.children[1])
            return (first / second) to (status0 || status1)
        }
        // g0(!A) = !(A / g0(A))
        "!e" -> {
            e as NotPredicate
            if (e.child is IdentToken) {
                val (transformed, status) = g0WithStatus(e.child)
                return !(e.child / transformed) to !status
            }
        }
    }
    if (e is Container) {
        e.replaceElements(this) { rules: Rules, token: Token -> rules.g0(token) }
    }
    return e to false
}

/**
 * @return ε-only part of [e] which returns the same result on all successful non-consuming input and fails otherwise.
 */
fun Rules.g0(e: Token): Token {
    return g0WithStatus(e).first
}

/**
 * ε-free part of [e] which returns same result on all successful consuming input and otherwise fails
 */
fun Rules.g1(e: Token): Token {
    when (e.getRepr(1)) {
        // g1(ε) = F
        "ε" -> return Token.fail()
        // g0(a) = a
        "a" -> return e
        // g1(A) = A
        "N" -> return e
        // g1(AB) = g0(A)B / A g0(B) / AB
        "(e e)" -> {
            if ((e as Group).getRepr(2) == "NN") {
                val (A, B) = e.children
                return (g0(A) and B) / (A and g0(B)) / (A and B)
            }
        }
        // g1(e1 / e2) = g1(e1) / g1(e2)
        "e / e" -> {
            e as Or
            return g1(e.children[0]) / g1(e.children[1])
        }
        // g1(!e) = F
        "!e" -> return Token.fail()
    }
    if (e is Container) {
        e.replaceElements(this) { rules: Rules, token: Token -> rules.g1(token) }
    }
    return e
}

private fun Rules.getRule(token: Token): Token {
    return this[(token as IdentToken).toIdent()]!!.child
}
