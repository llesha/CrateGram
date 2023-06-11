package reductions

import InterpreterError
import Rules
import generateNonTerminalName
import token.*

/**
 * Make repetition-free grammar
 * * e*: A <- e A/ε
 * * e+ <- e e*
 * * e? <- e / ε
 */
fun Rules.removeQuantifier(quantifier: Suffix): Token {
    return when (quantifier) {
        is QuestionMark -> Or(quantifier.child, Token.empty())
        // this code differs from original paper to make ast
        is Plus -> {
            val ruleFirst = generateNonTerminalName(this)
            val ruleSecond = generateNonTerminalName(this)
            this[ruleFirst] = Rule(Group(quantifier.child, ruleSecond))
            this[ruleSecond] = Rule(Or(Group(quantifier.child, ruleSecond), Token.empty()))
            GeneratedToken(ruleFirst.symbol, "+")
        }

        is Star -> {
            val ruleName = generateNonTerminalName(this)
            this[ruleName] = /* warning: duplicating [ruleName] */
                Rule(Or(Group(quantifier.child, ruleName), Token.empty()))
            GeneratedToken(ruleName.symbol, "*")
        }

        is Repeated -> {
            val children = mutableListOf<Token>()
            repeat(quantifier.quantity) {
                /* warning: duplicating [quantifier.child] */
                children.add(quantifier.child)
            }
            val ruleName = generateNonTerminalName(this)
            this[ruleName] = Group(children.toMutableList()).toRule()
            GeneratedToken(ruleName.symbol, "{...}")
        }

        else -> throw InterpreterError("unexpected token $quantifier")
    }
}
