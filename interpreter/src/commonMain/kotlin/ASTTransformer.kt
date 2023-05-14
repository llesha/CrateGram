import token.*

class ASTTransformer(val rules: MutableMap<IdentToken, Rule>) {
    fun transformRules(): MutableMap<IdentToken, Rule> {
        val initialKeys = rules.keys.toSet()
        for (key in initialKeys) {
            val newValue = transformRule(rules[key]!!)
            rules[key] = newValue
        }
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
        } else if (token is OneChildToken) {
            token.child = transformToken(token.child)
            if (token is Suffix)
                return transformQuantifier(token)
        }
        if (token is Group) {
            if (token.children.size == 1)
                return token.children.first()
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
            is QuestionMark -> Or(mutableListOf(quantifier.child, Token.empty()))
            is Plus -> Group(
                mutableListOf(quantifier.child, transformQuantifier(Star(quantifier.range, quantifier.child))),
                // warning: duplicating a token
                quantifier.range
            )

            is Star -> {
                val ruleName = generateNonTerminalName(rules.keys)
                rules[ruleName] =
                    Rule(
                        Or(
                            mutableListOf(
                                Group( /* warning: duplicating [ruleName] */mutableListOf(quantifier.child, ruleName)
                                ), Token.empty()
                            )
                        )
                    )
                ruleName
            }

            else -> throw InterpreterError("Unexpected token $quantifier")
        }
    }
}