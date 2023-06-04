/**
 *
 */
package reductions

import Rules
import generateNonTerminalName
import token.*

/**
 * add T, Z, F rules, apply [f]
 */
fun stage1(rules: Rules) {
    //addNewRules(rules)

    //  do {
    val keysFixed = rules.keys.toSet()
    for (key in keysFixed)
        rules[key] = rules.f(rules[key]!!.child).toRule()
    val keysDiff = rules.keys - keysFixed
    //  } while(keysDiff.isNotEmpty())
}

fun addNewRules(rules: Rules) {
    rules[Token.anyTerminal()] = Rule(AnyToken())
    rules[Token.any()] = Or(Group(Token.anyTerminal(), Token.any()), Token.empty()).toRule()
    rules[Token.fail()] = Group(Token.any(), Token.anyTerminal()).toRule()
    rules[Token.emptyRule()] = Literal("").toRule()
}

/**
 * * f(e1 e2) = AB; A <- e1; B <- e2
 * * f(e1 / e2) = A / !A f(e2); A <- f(e1)
 * * f(!e) = !A; A <- f(e)
 * * f(e) <- e; in other cases
 */
private fun Rules.f(e: Token): Token {
    when (e.getRepr(1)) {
        "(e e)" -> {
            e as Group
            val first = makeIdentIfNot(e.children[0], this)
            val second = makeIdentIfNot(e.children[1], this)
            if (isNewIdent(first, this))
                this[first] = this.f(e.children.first()).toRule()
            if (isNewIdent(second, this))
                this[second] = this.f(e.children.last()).toRule()
            e.children[0] = first
            e.children[1] = second
        }

        "e / e" -> {
            e as Or
            val first = makeIdentIfNot(e.children[0], this)
            val second = makeIdentIfNot(e.children[1], this)
            if (isNewIdent(first, this))
                this[first] = f(e.children.first()).toRule()
            if (isNewIdent(second, this))
                this[second] = this.f(e.children.last()).toRule()
            e.children[0] = first
            e.children[1] = Group(NotPredicate(first), second)
        }

        "!e" -> {
            e as NotPredicate
            if (e.child is IdentToken)
                return e
            val name = generateNonTerminalName(this)
            this[name] = this.f(e.child).toRule()
            e.child = name
        }

        else -> {
            if (e == Token.empty())
                return Token.emptyRule()
        }
    }
    return e
}

private fun isNewIdent(token: Token, rules: Rules): Boolean = token is IdentToken
        && rules[token.toIdent()]?.child == Literal("<TEMP>")

private fun makeIdentIfNot(e: Token, rules: Rules) = if (e is IdentToken) e
else if (e is Literal && e.symbol == "") Token.emptyRule()
else generateNonTerminalName(rules)
