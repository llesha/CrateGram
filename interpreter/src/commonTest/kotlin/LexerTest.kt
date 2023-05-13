import token.AndPredicate
import token.Prefix
import token.Star
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LexerTest {
    @Test
    fun testLexer() {
        val text = """a = ("A") | /B/"""
        Lexer(text).step()
    }

    @Test
    fun testOneRule() {
        val lexer = Lexer(
            """
            i = ("A")
        """.trimIndent()
        )
        lexer.step()
        assertTrue(lexer.rules.size == 1)
        assertTrue(lexer.rules.first().toString() == "i = (\"A\")")
    }

    @Test
    fun testSimplestRule() {
        val lexer = Lexer("a = b")
        val tokens = lexer.tokenize()
        assertEquals(tokens.joinToString("\n"), "[a, =, b]\n")
        //assertTrue(lexer.rules.size == 1)
        //assertTrue(lexer.rules.first().toString() == "a = b")
    }

    @Test
    fun testComments() {
        val lexer = Lexer(
            """
            Begin = "(*"
            End   = "*)"
            C     = Begin N* End
            N     = C | (!Begin !End Z)
            Z     = /[A-Z]/
        """.trimIndent()
        )
        lexer.step()
        assertTrue(lexer.rules.size == 5)
        assertTrue(
            lexer.rules.joinToString(separator = "\n") == """Begin = "(*"
End = "*)"
C = Begin N* End
N = C | (!Begin !End Z)
Z = /[A-Z]/"""
        )
    }


    @Test
    fun testCalculator() {
        val lexer = Lexer(
            """
            Expr    = Sum
            Sum     = Product (("+" | "-") Product)*
            Product = Power (("*" | "/") Power)*
            Power   = Value ("^" Power)?
            Value   = [0-9]+ | "(" Expr ")"
            """
        )
        val tokens = lexer.tokenize()
        assertEquals(tokens.joinToString(separator = "\n"), """[Expr, =, Sum]
[Sum, =, Product, (, (, "+", |, "-", ), Product, ), *]
[Product, =, Power, (, (, "*", |, "/", ), Power, ), *]
[Power, =, Value, (, "^", Power, ), ?]
[Value, =, /[0-9]+/, |, "(", Expr, ")"]
""")
        //lexer.step()
//        assertTrue(lexer.rules.size == 5)
//        assertTrue(
//            lexer.rules.joinToString(separator = "\n") == """Expr = Sum
//Sum = Product (("+" | "-") Product)*
//Product = Power (("*" | "/") Power)*
//Power = Value ("^" Power)?
//Value = /[0-9]+/ | "(" Expr ")""""
//        )
    }

    @Test
    fun testPrecedence() {
        val lexer = Lexer(
            """
            Expr = &a*
            """
        )
        lexer.step()
        assertTrue(lexer.rules.size == 1)
        assertTrue(lexer.rules.joinToString(separator = "\n") == "Expr = &a*")
        val rule = lexer.rules.first().rule
        assertTrue(rule.children.first() is AndPredicate)
        assertTrue((rule.children.first() as Prefix).child is Star)
        assertTrue(((rule.children.first() as Prefix).child as Star).child.symbol == "a")
    }
}
