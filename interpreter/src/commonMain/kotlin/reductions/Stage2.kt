package reductions

import Rules
import assert
import token.*

/**
 * Split grammar into [g0] and [g1]
 */
fun stage2(rules: Rules) {
    val ruleTokens = rules.values.toMutableList()
    for (i in ruleTokens.indices) {
        val tokenRule = ruleTokens[i].child
        println(rules.g1(tokenRule))
        println(rules.g0(tokenRule))
        ruleTokens[i].child = h1(rules.g1(tokenRule)) / h0(rules.g0(tokenRule), Token.anyTerminal())
    }
}

private fun Rules.g0WithStatus(e: Token): Pair<Token, Boolean> {
    when (e) {
        // g0(ε) = ε, g0(a) = F
        is Literal -> return if (e == Token.empty()) e to true else Token.fail() to false
        // g0(A) = R_G(A)  (to expression that is defined by rule A)
        is IdentToken, is GeneratedToken -> return g0WithStatus(getRule(e))
        //
        is Group -> {
            //assert(e.children[0] is IdentToken, e.children[1] is IdentToken)
            val first = g0WithStatus(e.children[0])
            if (first.second)
                return (first.first and g0WithStatus(e.children[1]).first) to true
            return Token.fail() to false
        }
        // g0(e1 / e2) = g0(e1) / g0(e2)
        is Or -> {
            val (first, status0) = g0WithStatus(e.children[0])
            val (second, status1) = g0WithStatus(e.children[1])
            return (first / second) to (status0 || status1)
        }
        // g0(!A) = !(A / g0(A))
        is NotPredicate -> {
            assert(e.child is IdentToken)
            val (transformed, status) = g0WithStatus(e.child)
            return !(e.child / transformed) to !status
        }

        is AnyToken -> return Token.fail() to false

        else -> throw Exception("Unexpected token $e")
    }
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
    return when (e) {
        // g1(ε) = F, g0(a) = a
        is Literal -> if (e == Token.empty()) Token.fail() else e
        // g1(A) = A
        is IdentToken -> e
        is Group -> {
            val (A, B) = e.children
            (g0(A) and B) / (A and g0(B)) / (A and B)
        }
        // g1(A / B) = g1(A) / g1(B)
        is Or -> g1(e.children[0]) / g1(e.children[1])
        // g1(!e) = F
        is NotPredicate -> Token.fail()
        is AnyToken -> e
        else -> throw Exception("$e")
    }
}

private fun Rules.getRule(token: Token): Token {
    return this[(token as IdentToken).toIdent()]!!.child
}