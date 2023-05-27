import result.Pipeline
import kotlin.test.Test

class AstTest {
    @Test
    fun testAstTransform() {
        val pipeline = Pipeline().setGrammar(
            """
            # Hello world
            root =   welcome COMMA _SPACE* subject punctuation !.

            welcome = ("Hello" | "Greetings" | "Salute") _SPACE*
            subject = [A-Z][a-z]* _SPACE*
            punctuation = [!?.] _SPACE*


            COMMA = "," 
            _SPACE = " "
        """.trimIndent()
        )
        pipeline.parse("Hello,    Alex!")
        val ast = pipeline.getAst()
        println(ast.toJson())
    }
}