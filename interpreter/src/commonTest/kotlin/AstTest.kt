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

    @Test
    fun testEmptyChildren() {
        setGrammar("""
            # equal amount of 0 and 1
            root = (rep / rep2)* "1"* !.
            # 0^n1^n
            rep = "0" rep "1" / "0" "1"
            # 1^n0^n
            rep2 = "1" rep2 "0" / "1" "0"
        """.trimIndent())

        assertParse("1010", true)
        println(pipeline.getAst())
    }
}
