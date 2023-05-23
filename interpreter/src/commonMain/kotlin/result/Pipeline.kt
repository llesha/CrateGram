import result.Interpreter
import token.IdentToken
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
@JsExport
class Pipeline {
    private val interpreter = Interpreter(mutableMapOf())


    fun setGrammar(grammar: String): Pipeline {
        val rules = Parser(Lexer(grammar).tokenize()).parse()
        val astTransformer = ASTTransformer(rules)
        astTransformer.transformRules()
        astTransformer.rules[IdentToken("root")] ?: throw InterpreterError("`root` rule is required")
        interpreter.rules.putAll(astTransformer.rules)

        return this
    }

    fun parse(text: String): Pair<Boolean, Int> {
        return interpreter.parseInput(text)
    }
}
