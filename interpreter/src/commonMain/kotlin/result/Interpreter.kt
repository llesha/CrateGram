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
    private val initialToken: Token
    val steps = mutableListOf<Step>()

    init {
        initialToken = rules[IdentToken("root")] ?: throw InterpreterError("`root` rule is required")
        steps.add(Step(initialToken, 0))
    }

    fun parseInput(text: String) {

    }

    fun nextStep() {

    }
}
