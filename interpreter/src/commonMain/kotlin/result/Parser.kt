package result

import ParserError
import listRange
import subList
import token.*

class Parser(private val rules: List<List<Token>>) {
    private var index = 0
    private val currentRule: MutableList<Token> = mutableListOf()
    private val stack: MutableList<Pair<String, Int>> = mutableListOf()
    fun parse(): MutableMap<IdentToken, Rule> {
        val res = mutableMapOf<IdentToken, Rule>()
        for (rule in rules) {
            index = 2
            currentRule.clear()
            val pair = parseRule(rule)
            res[pair.first] = pair.second
        }
        return res
    }

    private fun parseRule(rule: List<Token>): Pair<IdentToken, Rule> {
        makeRuleChecks(rule)
        val ident = rule.first() as IdentToken

        while (true) {
            currentRule.add(parseNext(rule, "~") ?: break)
        }
        val ruleToken = if (stack.isNotEmpty()) {
            val res = addOr()
            if (stack.isNotEmpty())
                throw ParserError("Parenthesis does not have a closing pair")
            res
        } else if (currentRule.size == 1) currentRule.first()
        else Group.fromList(currentRule.toList())
        return ident to Rule(ruleToken)
    }

    private fun makeRuleChecks(rule: List<Token>) {
        if (rule.size < 3)
            throw ParserError("Expected at least three tokens for rule: ident, =, expression", rule.listRange())
        if (rule[1].symbol != "=")
            throw ParserError("Expected = as second token, got ${rule[1]}", rule[1].range)
        if (rule.first() !is IdentToken)
            throw ParserError("Expected identifier as non-terminal name")
    }

    private fun parseUntil(tokens: List<Token>, condition: String, addToRule: Boolean = true) {
        while (true) {
            currentRule.add(parseNext(tokens, condition) ?: break)
        }
//        if (index >= tokens.size)
//            throw ParserError("End of text when expecting $condition", tokens.last().range)
    }

    private fun parseNext(tokens: List<Token>, condition: String): Token? {
        if (index >= tokens.size || (tokens[index].symbol in condition && tokens[index] is TempToken))
            return null
        return when (tokens[index]) {
            is TempToken -> {
                when (tokens[index].symbol) {
                    "(" -> {
                        val idx = currentRule.lastIndex + 1
                        val parenIdx = index++
                        stack.add("(" to idx)
                        parseUntil(tokens, ")")
                        if (index >= tokens.size)
                            throw ParserError("Parenthesis does not have a closing pair", tokens[parenIdx].range)
                        val children = if (stack.last().first == "|") {
                            mutableListOf(addOr())
                        } else {
                            val res = currentRule.subList(idx)
                            while (currentRule.size > idx)
                                currentRule.removeLast()
                            res
                        }
                        stack.removeLast()
                        index++
                        Group.fromList(children.toList())
                    }

                    "|", "/" -> {
                        stack.add("|" to currentRule.lastIndex + 1)
                        index++
                        parseUntil(tokens, condition.replace("|", "") + "|")
                        // TODO: rewrite algorithm to remove this token creation
                        TempToken("$")
                    }

                    "&" -> NotPredicate(NotPredicate(sureParseNext(tokens, condition), index..index), index..index)
                    "!" -> NotPredicate(sureParseNext(tokens, condition), index..index)

                    "*" -> checkForRuleAndPrefix(Star(index..index++, surePopPrevious(tokens[index - 1])))
                    "+" -> checkForRuleAndPrefix(Plus(index..index++, surePopPrevious(tokens[index - 1])))
                    "?" -> checkForRuleAndPrefix(QuestionMark(index..index++, surePopPrevious(tokens[index - 1])))
                    else -> throw ParserError("Unexpected token ${tokens[index]}", tokens[index].range)
                }
            }

            is Repeated -> {
                (tokens[index] as Repeated).child = surePopPrevious(tokens[index - 1])
                val res = checkForRuleAndPrefix(tokens[index] as Repeated)
                index++
                res
            }

            is AnyToken, is IdentToken, is Literal, is CharacterClass -> tokens[index++]
            else -> throw ParserError("Unexpected token ${tokens[index]}", tokens[index].range)
        }
    }

    private fun addOr(): Or {
        var children = mutableListOf<Token>()
        while (stack.lastOrNull()?.first == "|") {
            children.add(makeGroupFromStack())
            stack.removeLast()
        }
        if (stack.lastOrNull() == null) {
            children.add(Group.fromList(currentRule.toList()))
            currentRule.clear()
        } else {
            children.add(makeGroupFromStack())
        }
        children = children.reversed().toMutableList()
        return Or.fromList(children.toList())
    }

    private fun makeGroupFromStack(): Token {
        val groupChildren = currentRule.subList(stack.last().second).toMutableList()
        while (currentRule.size > stack.last().second) {
            currentRule.removeLast()
        }
        return Group.fromList(groupChildren.toList())
    }

    private fun checkForRuleAndPrefix(suffix: Suffix): Token {
        if (suffix.child is Prefix) {
            val prefix = suffix.child as Prefix
            val realChild = prefix.child
            suffix.child = realChild
            prefix.child = suffix
            return prefix
        }
        return suffix
    }

    private fun sureParseNext(tokens: List<Token>, condition: String): Token {
        index++
        return parseNext(tokens, condition) ?: throw ParserError(
            "Expected valid token after ${tokens[index - 1]}",
            tokens[index - 1].range
        )
    }

    private fun surePopPrevious(token: Token): Token {
        return currentRule.removeLastOrNull() ?: throw ParserError("Expected token before $token", token.range)
    }
}
