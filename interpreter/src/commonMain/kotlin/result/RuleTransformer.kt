package result

import InterpreterError
import ParserError
import generateNonTerminalName
import token.*

class RuleTransformer(val rules: MutableMap<IdentToken, Rule>) {
    private val ruleNames = mutableSetOf<IdentToken>()
    fun transformRules(): MutableMap<IdentToken, Rule> {
        val initialKeys = rules.keys.toSet()
        for (key in initialKeys) {
            val newValue = transformRule(rules[key]!!)
            rules[key] = newValue
        }
        addNewRules()
        return rules
    }

    private fun transformRule(rule: Rule): Rule {
        rule.child = transformToken(rule.child)
        return rule
    }

    private fun transformToken(token: Token): Token {
        if (token is Expression) {
            val newChildren = token.children.filter { it !is TempToken }
            token.children.clear()
            token.children.addAll(newChildren.map { transformToken(it) })
            if (token.children.size < 2 && token is Or) {
                throw ParserError("Expected two children or more in Or expression", token.range)
            }
            if (token.children.size == 1) {
                return token.children.first()
            }
            if(token.children.isEmpty())
                throw ParserError("Empty expression", token.range)
        } else if (token is OneChildToken) {
            token.child = transformToken(token.child)
            if (token is Suffix)
                return transformQuantifier(token)
        } else if (token is IdentToken && !rules.keys.contains(token)) {
            throw ParserError("No rule named $token", token.range)
        }
        return token
    }

    /**
     * e*: A <- e A/ε
     * e+ <- e e*
     * e? <- e / ε
     */
    private fun transformQuantifier(quantifier: Suffix): Token {
        return when (quantifier) {
            is QuestionMark -> Or(quantifier.child, Token.empty())
            // this code differs from original paper to make ast
            is Plus -> {
                val ruleFirst = generateNonTerminalName(ruleNames)
                val ruleSecond = generateNonTerminalName(ruleNames)
                rules[ruleFirst] = Rule(Group(quantifier.child, ruleSecond))
                rules[ruleSecond] = Rule(Or(Group(quantifier.child, ruleSecond), Token.empty()))
//                Group(
//                    quantifier.child, transformQuantifier(Star(quantifier.range, quantifier.child)),
//                    // warning: duplicating a token
//                    quantifier.range
//                )
                GeneratedToken(ruleFirst.symbol, "+")
            }

            is Star -> {
                val ruleName = generateNonTerminalName(ruleNames)
                rules[ruleName] = /* warning: duplicating [ruleName] */
                    Rule(Or(Group(quantifier.child, ruleName), Token.empty()))
                GeneratedToken(ruleName.symbol, "*")
            }

            is Repeated -> {
                val children = mutableListOf<Token>()
                repeat(quantifier.quantity) {
                    /* warning: duplicating [quantifier.child] */
                    children.add(quantifier.child)
                }
                val ruleName=  generateNonTerminalName(ruleNames)
                rules[ruleName] =   Group(children.toMutableList()).toRule()
                GeneratedToken(ruleName.symbol, "{...}")
            }

            else -> throw InterpreterError("Unexpected token $quantifier")
        }
    }

    private fun addNewRules() {
//        rules[IdentToken("\$T")] = Rule(AnyToken())
//        rules[IdentToken("\$Z")] =
//            Or(Group(IdentToken("\$T"), IdentToken("\$Z")), Token.empty()).toRule()
//        rules[IdentToken("\$F")] = Group(IdentToken("\$Z"), IdentToken("\$T")).toRule()

//        val keysFixed = rules.keys.toSet()
//        for (key in keysFixed)
//            rules[key] = introduceNewRulesInStage1(rules[key]!!.child).toRule()
    }

    private fun introduceNewRulesInStage1(token: Token): Token {
        when (token) {
            is Group -> {
                val first = if (token.children[0] is IdentToken) token.children[0] as IdentToken
                else generateNonTerminalName(ruleNames)
                val second = if (token.children[1] is IdentToken) token.children[1] as IdentToken
                else generateNonTerminalName(ruleNames)
                if (isNewIdent(first))
                    rules[first] = introduceNewRulesInStage1(token.children.first()).toRule()
                if (isNewIdent(second))
                    rules[second] = introduceNewRulesInStage1(token.children.last()).toRule()
                token.children[0] = first
                token.children[1] = second
            }

            is Or -> {
                val first = if (token.children[0] is IdentToken) token.children[0] as IdentToken
                else generateNonTerminalName(ruleNames)
                if (isNewIdent(first))
                    rules[first] = introduceNewRulesInStage1(token.children.first()).toRule()
                token.children[0] = first
                token.children[1] = Group(NotPredicate(first), introduceNewRulesInStage1(token.children[1]))
            }

            is NotPredicate -> {
                if (token.child is IdentToken)
                    return token
                val name = generateNonTerminalName(ruleNames)
                rules[name] = introduceNewRulesInStage1(token.child).toRule()
                token.child = name
            }
        }
        return token
    }

    private fun removeNotPredicate() {

    }

    private fun isNewIdent(token: IdentToken): Boolean = token.symbol.startsWith("$")
            && token.symbol[1] !in mutableListOf('Z', 'T', 'F')
}