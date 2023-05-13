package result

import token.Or
import token.Rule
import token.RuleWithName

/**
 * https://pdos.csail.mit.edu/~baford/packrat/popl04/peg-popl04.pdf
 * ## Definitions for reduction
 * * ε - empty string
 * * a - any terminal in V_T
 * * A - any non-terminal
 * * e1e2 - sequence
 * * e1/e2 - choice
 * * e* - 0+ repetitions
 * * !e - not-predicate
 */
class Interpreter(grammarRules: MutableList<RuleWithName>) {
    val rules = mutableMapOf<String, Rule>()
    private val initialToken: Or
    val steps = mutableListOf<Step>()

    init {
        rules.putAll(grammarRules.associate { it.name to it.rule })
        initialToken = Or(0..0, grammarRules.map { it.rule }.toMutableList())
        steps.add(Step(initialToken, 0))
        val a = arrayOf(1, 2, 3, 1, null, null)
    }

    fun parseInput(text: String) {

    }

    fun nextStep() {

    }
}