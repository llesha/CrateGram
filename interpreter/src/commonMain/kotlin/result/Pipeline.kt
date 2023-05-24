package result

import InterpreterError
import token.IdentToken
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName


@OptIn(ExperimentalJsExport::class)
@JsExport
class Pipeline {
    private val interpreter = Interpreter(mutableMapOf())


    @JsName("setGrammar")
    fun setGrammar(grammar: String): Pipeline {
        val rules = Parser(Lexer(grammar).tokenize()).parse()
        val ruleTransformer = RuleTransformer(rules)
        ruleTransformer.transformRules()
        ruleTransformer.rules[IdentToken("root")] ?: throw InterpreterError("`root` rule is required")
        interpreter.rules.putAll(ruleTransformer.rules)

        return this
    }

    @JsName("parse")
    fun parse(text: String): Array<Any> {
        val (isParsed, index) =  interpreter.parseInput(text)
        return arrayOf(isParsed, index)
    }
}
