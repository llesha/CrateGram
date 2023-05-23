import result.Pipeline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class InterpreterTest {

    @Test
    fun testOneRuleGrammar() {
        val pipeline = Pipeline().setGrammar("""root = 'A'""")
        assertParseResult(pipeline.parse("A"), mutableListOf(true, 1))
    }

    @Test
    fun testSimpleGrammar() {
        val pipeline = Pipeline().setGrammar("""root = ("a"+ | "b") [AB]""".trimMargin())
        assertParseResult(pipeline.parse("bA"), mutableListOf(true, 2))
        assertParseResult(pipeline.parse("bB"), mutableListOf(true, 2))

        assertParseResult(pipeline.parse("aA"), mutableListOf(true, 2))
        assertParseResult(pipeline.parse("aaaaaA"), mutableListOf(true, 6))
        assertParseResult(pipeline.parse("aaaaB"), mutableListOf(true, 5))
        assertParseResult(pipeline.parse("B"), mutableListOf(false, 0))
    }

    @Test
    fun testParentheses() {
        val pipeline = Pipeline().setGrammar(
            """
            root = ([0-9]+ | "(" [ABC] ")") " "*
        """
        )
    }

    @Test
    fun testNumbers() {
        val pipeline = Pipeline().setGrammar(
            """
            Value   = ([0-9]+ | "(" Expr ")") Space
            root    = Expr
            Expr    = Sum
            Sum     = Product (("+" | "-") Product)*
            Product = Power (("*" | "/") Power)*
            Power   = Value ("^" Power)? 
            Space = " "*
            """
        )
        println(pipeline.parse("12"))
        println(pipeline.parse("(12)"))
        println(pipeline.parse("(12+23*3^3)"))
    }

    @Test
    fun testEmptyOr() {
        val exception = assertFails { Pipeline().setGrammar("root = [ABCDE]/") }
        assertTrue((exception as PosError).msg.contains("Expected two children"))
    }

    @Test
    fun testLoop() {
        val pipeline = Pipeline().setGrammar(
            """
                # line comment
            Value   = [0-9.]+ / "(" Expr ")"
            # another line comment
            Product = Expr (("*" / "/") Expr)*
            Sum     = Expr (("+" / "-") Expr)*
            Expr    = "a" Product / Sum / Value
            root = Expr
        """
        )
        val exception = assertFails { pipeline.parse("2343") }
        assertTrue(exception.toString().lowercase().contains("stack"))
    }

    private fun assertParseResult(parseResult: Array<Any>, expected: List<Any>) {
        assertEquals(parseResult[0] as Boolean, expected[0] as Boolean)
        assertEquals(parseResult[1] as Int, expected[1] as Int)
    }
}