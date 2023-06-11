import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PositionTest {
    @Test
    fun testEscapedTokenPositionErrors() {
        assertPositionCase("root = [\\g]", '\\', 2)
        assertPositionCase("root = \"\\]\"", '\\', 2)
    }

    @Test
    fun testEscapedUnicodeCharacter() {
        assertPositionCase("root = \"\\u123r\"", '\\', 6)
        assertPositionCase("root = [\\u123]", '\\', 5)
    }

    @Test
    fun testNotFoundRule() {
        assertPositionCase("root = a", 'a', 1)
    }

    @Test
    fun testIncorrectRule() {
        assertPositionCase("root = = a", 'r', 4)
    }

    private fun assertPositionCase(grammar: String, startChar: Char, length: Int) {
        val posError = assertFails { setGrammar(grammar) } as PosError
        assertEquals(grammar.indexOf(startChar).rangeOfLength(length), posError.range)
    }
}
