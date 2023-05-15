import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class LexerTest {
    @Test
    fun testLexer() {
        val text = """a = ("A") | [AB]"""
        val rules = Parser(Lexer(text).tokenize()).parse()
        ASTTransformer(rules).transformRules()
        println(rules)
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
            Z     = [A-Z]
        """.trimIndent()
        )
//        lexer.step()
//        assertTrue(lexer.rules.size == 5)
//        assertTrue(
//            lexer.rules.joinToString(separator = "\n") == """Begin = "(*"
//End = "*)"
//C = Begin N* End
//N = C | (!Begin !End Z)
//Z = /[A-Z]/"""
//        )
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
        assertEquals(exception.message, "Unclosed multiline comment at 107..108")
    }


    @Test
    fun testCalculator() {
        val lexer = Lexer(
            """
            Power   = Value ("^" Power)? (* comment here
            *)
            Sum     = Product (("+" | "-") Product)*
            Expr    = Sum
            Product = Power (("*" | "/") Power)*
            Value   = [0-9]+ | "(" Expr ")"
            Test    = "A" / &Value{3}
            """
        )
        val tokens = lexer.tokenize()
//        assertEquals(
//            tokens.joinToString(separator = "\n"), """[Expr, =, Sum]
//[Sum, =, Product, (, (, "+", |, "-", ), Product, ), *]
//[Product, =, Power, (, (, "*", |, "/", ), Power, ), *]
//[Power, =, Value, (, "^", Power, ), ?]
//[Value, =, ["0-9"], +, |, "(", Expr, ")"]"""
//        )
        val parser = Parser(tokens)
        val rules = parser.parse()
        val transformer = ASTTransformer(rules)
        transformer.transformRules()
        println(rules.toList().joinToString(separator = "\n"))
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
