package tasks

import TestFactory.assertParse
import setGrammar
import kotlin.test.Test


class BinaryTest {
    @Test
    fun testB2() {
        setGrammar(
            """root = block* !.
block = "0011" | "0101" | "0110" | "1001" | "1010" | "1100"
"""
        )

        assertParse("1", false)
    }

    @Test
    fun testB5() {
        setGrammar(
            """
            root = (z / o)* !.
            z = "0" (z / o) "1" / "01"
            o = "1" (z / o) "0" / "10"
        """
        )
        assertParse("00110110", true)
        assertParse("0011110100110100110010", true)

        assertParse("0", false)
        assertParse("a", false)
    }
}
