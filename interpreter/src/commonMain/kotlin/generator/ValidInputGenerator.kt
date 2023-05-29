package generator

import random
import token.*

class ValidInputGenerator(val rules: Map<IdentToken, Rule>) {
    fun generateToken(token: Token): String {
        when (token) {
            is Literal -> return token.symbol
            is CharacterClass -> return token.getRandomVariant()
            is IdentToken -> return generateToken(rules[token] ?: throw Exception("Token $token not found in rules"))
            is Or -> return generateToken(token.children.random(random))
            is Group -> {
                val res = StringBuilder()
                for (child in token.children)
                    res.append(generateToken(child))
                return res.toString()
            }

            is NotPredicate -> {
                return "A"
            }

            is AnyToken -> return (('A'..'Z') + ('a'..'z') + ('0'..'9') + '_').random(random).toString()
            else -> throw Exception("Unknown token")
        }
    }
}