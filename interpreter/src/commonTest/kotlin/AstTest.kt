import TestFactory.assertParse
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

    @Test
    // TODO: bad ast
    fun simpleAst() {
        setGrammar(
            """
            root = a "1"? !.
            a = "10"*"""
        )
        assertParse("", true)
        assertParse("1", true)
        assertParse("10", true)
        assertParse("101", true)

        assertParse("11", false)
        assertParse("00", false)
    }
}
