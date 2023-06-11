import TestFactory.assertParse
import result.Lexer
import kotlin.test.Test

class CharacterClassTest {
    @Test
    fun testCharacterClass() {
        val text = "a = [AB\\t\\n \\\\]"
        val tokens = Lexer(text).tokenize()
    }

    @Test
    fun testRange() {
        setGrammar("root = [a-z]")
        for (char in 'a'..'z') {
            assertParse(char.toString(), true)
        }
    }

    @Test
    fun testEscapedRange() {
        setGrammar("""root = [\b-\r]""")
        for (char in '\b'..'\r')
            assertParse(char.toString(), true)
        assertParse(Char('\r'.code + 1).toString(), false)

        setGrammar("""root = [\b-0]""")
        for (char in '\b'..'0')
            assertParse(char.toString(), true)
        assertParse(Char('0'.code + 1).toString(), false)
    }
}