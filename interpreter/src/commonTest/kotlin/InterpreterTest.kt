import result.Interpreter
import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {

    @Test
    fun testSimpleGrammar() {
        val pipeline = Pipeline("""root = ("a"+ | "b") [AB] """.trimMargin())
        assertEquals(pipeline.parse("bA"), true to 2)
        assertEquals(pipeline.parse("bB"), true to 2)

        assertEquals(pipeline.parse("aA"), true to 2)
        assertEquals(pipeline.parse("aaaaaA"), true to 6)
        assertEquals(pipeline.parse("aaaaB"), true to 5)
        assertEquals(pipeline.parse("B"), false to 0)
    }

    @Test
    fun testNumbers() {
        val pipeline = Pipeline("""
            root = Expr
            Expr    = Sum
            Sum     = Product (("+" | "-") Product)*
            Product = Power (("*" | "/") Power)*
            Power   = Value ("^" Power)? 
            Value   = [0-9]+ | "(" Expr ")"
            """)
        println(pipeline.parse("12"))
        println(pipeline.parse("(12)"))
        println(pipeline.parse("(12+23*3^3)"))
    }
}