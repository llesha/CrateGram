package token

import Rules

/**
 * Tokens that cannot be added into other tokens
 */
interface Container {
    fun getElements(): List<Token>

    fun replaceElements(rules: Rules, replace: (rules: Rules, token: Token) -> Token)
}