package result

import InterpreterError
import token.*

/**
 * [original article](https://pdos.csail.mit.edu/~baford/packrat/popl04/peg-popl04.pdf)
 * ## Definitions for reduction
 *  * e - parse expression
 *  * x, y, z - strings of terminals
 *  * G(V_N, V_T, R, e_S) - grammar with V_T terminals, V_N non-terminals, R rules and e_S starting expression
 *
 *
 * * ε - empty string
 * * A - any terminal in V_T
 * * a - any non-terminal
 * * e1e2 - sequence
 * * e1/e2 - choice
 * * e* - 0+ repetitions
 * * !e - not-predicate
 *
 * ## Desugaring:
 * 1. '.' - all in V_T
 * 2. [ABCD] <- A / B / C / D
 * 3. e? <- e / ε
 * 4. e+ <- e e*
 * 5. &e <- !(!e)
 *
 * ## Reduction:
 * * e* replaced with: a <- e a / ε
 * * e+ replaced with: a1 (not from original article to make ast work)
 * 1. a1 <- e a2
 * 2. a2 <- e a2 / ε
 *
 * ### Reducing predicates
 * Add non-terminals:
 * 1. t <- .
 * 2. z <- tz / ε # always succeeds
 * 3. f <- zt # always fails
 *
 * Therefore, reduced grammar is repetition-free and predicate-free, containing only:
 * 1. sequences e1e2
 * 2. choices e1 / e2
 *
 */
class Interpreter(val rules: MutableMap<IdentToken, Rule>) {
    /**
     * List of characters not recognized by [AnyToken]
     */
    private val dotExceptions = "\n\r\u2028\u2029"
    val ast = Node("")
    private var currentParent = ast

    fun parseInput(text: String): Pair<Boolean, Int> {
        ast.children.clear()
        currentParent = ast
        return followedBy(IdentToken("root"), text, 0)
    }

    /**
     * Check if in [token] is at [index] of [text]
     * @return (true and index after successful tokenizing) or (false and initial index before unsuccessful tokenizing)
     */
    private fun followedBy(token: Token, text: String, index: Int): Pair<Boolean, Int> {
        when (token) {
            is NotPredicate -> return withAst {
                val result = followedBy(token.child, text, index)
                return@withAst !result.first to index
            }

            is Group -> return withAst {
                var changedIndex = index
                for (child in token.children) {
                    val next = followedBy(child, text, changedIndex)
                    if (!next.first)
                        return@withAst false to index
                    changedIndex = next.second
                }
                return@withAst true to changedIndex
            }

            is Or -> return withAst {
                for (child in token.children) {
                    val result = followedBy(child, text, index)
                    if (result.first)
                        return@withAst true to result.second
                }
                return@withAst false to index
            }

            is Literal -> return withAstValue(text, index) {
                if (token.symbol == "")
                    return@withAstValue true to index
                if (token.symbol.length + index - 1 >= text.length)
                    return@withAstValue false to index
                if (text.substring(index, index + token.symbol.length) == token.symbol)
                    return@withAstValue true to index + token.symbol.length
                return@withAstValue false to index
            }

            is CharacterClass -> return withAstValue(text, index) {
                if (index >= text.length)
                    return@withAstValue false to index
                val variants = token.variants
                if (text[index] == '\\') {
                    if (index + 1 >= text.length)
                        return@withAstValue false to index
                    variants.ranges.forEach {
                        // TODO: create function that will create real escapes and then make ranges with them
                        if (it.firstEscaped && text[index + 1] in it.range)
                            return@withAstValue true to index + 2
                    }
                    return@withAstValue if (text[index] in variants.escaped)
                        true to index + 2
                    else
                        false to index
                }
                variants.ranges.forEach {
                    if (!it.firstEscaped && text[index] in it.range)
                        return@withAstValue true to index + 1
                }
                return@withAstValue if (text[index] in variants.normal)
                    true to index + 1
                else
                    false to index
            }

            is IdentToken, is GeneratedToken -> {
                val prevParent = currentParent
                if (token.symbol[0] != '$' || (token is GeneratedToken && token.inPlaceOfPrevious != null)) {
                    val newNode = if (token is GeneratedToken) Node(token.inPlaceOfPrevious!!) else Node(token.symbol)
                    currentParent.children.add(newNode)
                    currentParent = newNode
                }
                val result = withAst {
                    followedBy(
                        rules[IdentToken(token.symbol)]?.child ?: throw InterpreterError(
                            "No rule named $token",
                            position = index
                        ),
                        text,
                        index
                    )
                }
                currentParent = prevParent
                return result
            }

            is AnyToken -> return withAst {
                if (index >= text.length || text[index] in dotExceptions)
                    return@withAst false to index
                return@withAst true to index + 1
            }

            else -> throw InterpreterError(
                "Expected (), |, !, \"\", [] or rule name at interpretation stage",
                position = index
            )
        }
    }

    private fun withAstValue(text: String, beforeIndex: Int, block: () -> Pair<Boolean, Int>): Pair<Boolean, Int> {
        val prevParent = currentParent
        val childrenCount = prevParent.children.size
        val result = block()
        if (!result.first) {
            currentParent = prevParent
            while (currentParent.children.size != childrenCount)
                currentParent.children.removeLast()
        } else {
            currentParent.children.add(ValueNode(text.substring(beforeIndex, result.second)))
        }
        return result
    }

    private fun withAst(block: () -> Pair<Boolean, Int>): Pair<Boolean, Int> {
        val prevParent = currentParent
        val childrenCount = prevParent.children.size
        val result = block()
        if (!result.first) {
            currentParent = prevParent
            while (currentParent.children.size != childrenCount)
                currentParent.children.removeLast()
        }
        return result
    }
}
