package result

import InterpreterError
import reductions.transformRules
import token.IdentToken
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName


@OptIn(ExperimentalJsExport::class)
@JsExport
class Pipeline {
    val interpreter = Interpreter(mutableMapOf())


    @JsName("setGrammar")
    fun setGrammar(grammar: String): Pipeline {
        val rules = Parser(Lexer(grammar).tokenize()).parse()
        transformRules(rules)
        rules[IdentToken("root")] ?: throw InterpreterError("`root` rule is required", 0..1)
        interpreter.rules.putAll(rules)

        return this
    }

    @JsName("hasGrammar")
    fun hasGrammar(): Boolean {
        return interpreter.rules.isNotEmpty()
    }

    @JsName("clearGrammar")
    fun clearGrammar(): Pipeline {
        interpreter.rules.clear()

        return this
    }

    fun getAst(): Node {
        AstTransformer(interpreter.ast).transformAst()
        return interpreter.ast
    }

    @JsName("parse")
    fun parse(text: String): Array<Any> {
        val (isParsed, index) = interpreter.parseInput(text)
        return arrayOf(isParsed, index)
    }

    @JsName("setDotExceptions")
    fun setDotExceptions(newExceptions: String) {
        interpreter.dotExceptions = newExceptions
    }
}
