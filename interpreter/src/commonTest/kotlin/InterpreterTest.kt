import TestFactory.assertParse
import result.Pipeline
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class InterpreterTest {

    @Test
    fun testOneRuleGrammar() {
        setGrammar("""root = 'A'""")
        assertParse("A", true, 1)
    }

    @Test
    fun testSimpleGrammar() {
        setGrammar("""root = ("a"+ | "b") [AB]""".trimMargin())
        assertParse("bA", true, 2)
        assertParse("bB", true, 2)

        assertParse("aA", true, 2)
        assertParse("aaaaaA", true, 6)
        assertParse("aaaaB", true, 5)
        assertParse("B", false, 0)
    }

    @Test
    fun testParentheses() {
        setGrammar(
            """
            root = ([0-9]+ | "(" [ABC] ")") " "*
        """
        )

        assertParse("12", true)
        assertParse("(A)", true)
    }

    @Test
    fun testNumbers() {
        setGrammar(
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
        assertParse("12", true)
        assertParse("(12)", true)
        assertParse("(12+23*3^3)", true)
    }

    @Test
    fun testEmptyOr() {
        val exception = assertFails { Pipeline().setGrammar("root = [ABCDE]/") }
        assertTrue((exception as PosError).msg.contains("empty expression"))
    }

    @Test
    fun testRepeated() {
        setGrammar("""root = "A"{3}""")
        assertParse("AAA", true, 3)
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

    @Test
    fun testIncorrectGrammar() {
        setGrammar(
            """
            root = (no1+ | no4+) !.
            no1 = "2" | "3" | !"4"
            no4 = "1" | "2" | "3"
        """
        )
        val exception = assertFails { parse("231") }
        assertTrue(
            exception.toString().contains("java.lang.StackOverflowError")
                    || exception.toString().contains("RangeError: Maximum call stack size exceeded")
        )
    }

    @Test
    fun testStringRepresentation() {
        val pipeline = Pipeline().setGrammar(
            """
            root = choice !.

            choice = in4 | in3 | in2 | in1
            in4 = "4" (choice | TERM) "4"
            in3 = "3" (choice | TERM) "3"
            in2 = "2" (choice | TERM) "2"
            in1 = "1" (choice | TERM) "1"

            TERM = "1" | "2" | "3" | "4" | ""
        """.trimIndent()
        )
        pipeline.parse("11")
    }

    @Test
    fun testExtraIdents() {
        val exception = assertFails {
            setGrammar(
                """
            Hello world
            root = welcome COMMA SPACE* subject punctuation !.

            welcome = ("Hello" | "Greetings" | "Salute") SPACE*
            subject = [A-Z][a-z]* SPACE*

            punctuation = [!?.]
            COMMA = ","
            SPACE = [ ]
        """.trimIndent()
            )
        }
    }

    @Test
    fun testEmptyCharacterClass() {
        setGrammar(
            """
            root = welcome COMMA SPACE* subject punctuation !.

            welcome = ("Hello" | "Greetings" | "Salute") SPACE*
            subject = [A-Z][a-z]* SPACE*

            punctuation = [!?.]
            COMMA = ","
            SPACE = []
        """
        )

        assertParse("Hello,A!", true)

        assertParse("Hello, A!", false)
    }

    @Test
    fun testEscapedGrammar() {
        setGrammar("""root = "\"2\"" | "\n\r\b\t\'\\" """)
        assertParse(""""2"""", true)
        assertParse("\n\r\b\t\'\\", true)

        setGrammar("""root = [\]"]""")
        assertParse("""]""", true)
        assertParse(""""""", true)

        setGrammar("""root =  [\]\'\"\-\b\n\t\r] """)
        assertParse("]", true)
        assertParse("'", true)
        assertParse("\"", true)
        assertParse("-", true)
        assertParse("\b", true)
        assertParse("\n", true)
        assertParse("\t", true)
        assertParse("\r", true)

        assertParse("r", false)
    }
}
