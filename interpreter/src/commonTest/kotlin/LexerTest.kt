import reductions.transformRules
import result.Interpreter
import result.Lexer
import result.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class LexerTest {
    @Test
    fun testLexer() {
        val text = """a = ("A") "B" "C" "D" | [AB]"""
        val rules = Parser(Lexer(text).tokenize()).parse()
        transformRules(rules)
        println(rules.toList().joinToString(separator = "\n"))
    }

    @Test
    fun testOneRule() {
        val lexer = Lexer(
            """
            i = ("A")
        """.trimIndent()
        )
//        lexer.step()
//        assertTrue(lexer.rules.size == 1)
//        assertTrue(lexer.rules.first().toString() == "i = (\"A\")")
    }

    @Test
    fun testSimplestRule() {
        val lexer = Lexer("a = b")
        val tokens = lexer.tokenize()
        assertEquals(tokens.joinToString("\n"), "[a, =, b]")
    }

    @Test
    fun testComments() {
        val lexer = Lexer(
            """
            Begin = "(*"
            End   = "*)"
            C     = Begin N* End
            N     = C | (!Begin !End Z)
            Z     = [A-Z]
        """.trimIndent()
        )
        lexer.tokenize()
    }

    @Test fun testUnclosedComment() {
        val lexer = Lexer("""
            Power   = Value ("^" Power)?
            Sum     = Product (("+" | "-") Product)*
            (*
            Product = Power (("*" | "/") Power)*
            Value   = [0-9]+ | "(" Expr ")"
            Test    = "A" / &Value{3}
        """)
        val exception = assertFails { lexer.tokenize() }
        assertTrue(exception.stackTraceToString().contains("LexerError"))
    }


    @Test
    fun testCalculator() {
        val lexer = Lexer(
            """
            root = Power
            Power   = Value ("^" Power)? 
            Sum     = Product (("+" | "-") Product)*
            Expr    = Sum
            Product = Power (("*" | "/") Power)*
            Value   = [0-9]+ | "(" Expr ")"
            """
        )
        val tokens = lexer.tokenize()
        assertEquals(
            tokens.joinToString(separator = "\n"), """[root, =, Power]
[Power, =, Value, (, "^", Power, ), ?]
[Sum, =, Product, (, (, "+", |, "-", ), Product, ), *]
[Expr, =, Sum]
[Product, =, Power, (, (, "*", |, "/", ), Power, ), *]
[Value, =, [0-9], +, |, "(", Expr, ")"]"""
        )
        val parser = Parser(tokens)
        val rules = parser.parse()
        transformRules(rules)
        println(rules.toList().joinToString(separator = "\n"))
        val interpreter = Interpreter(rules)
        val res = interpreter.parseInput("1+2")
        println(res)
    }

    @Test
    fun testPrecedence() {
        val lexer = Lexer(
            """
            Expr = &a*
            """
        )
        val parser = Parser(lexer.tokenize())
        val rules = parser.parse()
        println(rules.toList().joinToString(separator = "\n"))
//        lexer.step()
//        assertTrue(lexer.rules.size == 1)
//        assertTrue(lexer.rules.joinToString(separator = "\n") == "Expr = &a*")
//        val rule = lexer.rules.first().rule
//        assertTrue(rule.children.first() is AndPredicate)
//        assertTrue((rule.children.first() as Prefix).child is Star)
//        assertTrue(((rule.children.first() as Prefix).child as Star).child.symbol == "a")
    }

    @Test
    fun testPalindrome() {
        val grammar = """
            inD   = "D" inC "D" / start
            root  = e !.
            e     = inD
            inC   = "C" inB "C" / start
            inB   = "B" inA "B" / start
            inA   = "A" inD "A" / start
            start = [ABCD]
        """
        val lexer = Lexer(grammar)
        val rulesUnparsed = lexer.tokenize()
        println(rulesUnparsed.joinToString(separator ="\n"))
        println()
        val parser = Parser(rulesUnparsed)
        val rules = parser.parse()
        println()
        println(rules.toList().joinToString(separator = "\n"))
    }
}
