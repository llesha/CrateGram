package tasks

import TestFactory.assertParse
import setGrammar
import kotlin.test.Test

class BinaryTest {
    @Test
    fun testB2() {
        setGrammar("""root = block* !.
block = "0011" | "0101" | "0110" | "1001" | "1010" | "1100"
""")

        assertParse("1", false)
    }
}