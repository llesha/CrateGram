package reductions

import ParserError
import Rules
import token.*
import subList

fun transformRules(rules: Rules): MutableMap<IdentToken, Rule> {
    val initialKeys = rules.keys.toSet()
    for (key in initialKeys) {
        val newValue = transformRule(rules[key]!!, rules)
        rules[key] = newValue
    }
    //addNewRules(rules)
    return rules
}

private fun transformRule(rule: Rule, rules: Rules): Rule {
    rule.child = transformToken(rule.child, rules)
    return rule
}

private fun transformToken(token: Token, rules: Rules): Token {
    if (token is Expression) {
        val newChildren = token.children.filter { it !is TempToken }
        token.children.clear()
        token.children.addAll(newChildren.map { transformToken(it, rules) })
        if (token.children.size < 2 && token is Or) {
            throw ParserError("Expected two children or more in Or expression", token.range)
        }
        if (token.children.size == 1) {
            return token.children.first()
        }
        if (token.children.isEmpty())
            throw ParserError("Empty expression", token.range)
    } else if (token is OneChildToken) {
        token.child = transformToken(token.child, rules)
        if (token is Suffix)
            return rules.removeQuantifier(token)
    } else if (token is IdentToken && !rules.keys.map { it.hashCode() }.contains(token.hashCode())) {
        throw ParserError("No rule named $token", token.range)
    }
    return token
}

fun expressionsToTwoChildren(token: Token): Token {
    when (token) {
        is Or -> {
            if (token.children.size == 1)
                return token.children[0]
            val res =  Or(
                expressionsToTwoChildren(token.children[0]),
                expressionsToTwoChildren(Or(token.children.subList(1)))
            )
            return res
        }

        is Group -> {
            if (token.children.size == 1)
                return token.children[0]
            return Group(
                expressionsToTwoChildren(token.children[0]),
                expressionsToTwoChildren(Group(token.children.subList(1)))
            )
        }

        is CharacterClass -> {
            return expressionsToTwoChildren(token.toOr())
        }

        is Suffix -> {
            token.child = expressionsToTwoChildren(token.child)
            return token
        }

        is Prefix -> {
            token.child = expressionsToTwoChildren(token.child)
            return token
        }

        else -> return token
    }
}
