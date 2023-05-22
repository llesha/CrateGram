import result.Interpreter

class Pipeline(private val grammar: String) {
    private val interpreter: Interpreter

    init {
        val rules = Parser(Lexer(grammar).tokenize()).parse()
        val astTransformer = ASTTransformer(rules)
        astTransformer.transformRules()
        interpreter = Interpreter(astTransformer.rules)
    }

    fun parse(text: String): Pair<Boolean, Int> {
        return interpreter.parseInput(text)
    }
}