package tasks

import TestFactory.assertParse
import setGrammar
import kotlin.test.Test
import pipeline

class CPLogicTest {
    @Test
    fun testNotReallyPalindrome() {
        setGrammar(
            """
            # palindrome
            root = choice !.

            choice = in4 | in3 | in2 | in1 | TERM
            in4 = "4" choice "4"
            in3 = "3" choice "3"
            in2 = "2" choice "2"
            in1 = "1" choice "1"

            TERM = "1" | "2" | "3" | "4" | ""
        """.trimIndent()
        )
        assertParse("", true, 0)
        assertParse("1", true, 1)
        assertParse("1112343213123432111", true, 19)

        assertParse("4344", false, 0)
        assertParse("55", false, 0)
        assertParse("431", false, 0)

        assertParse("44", false, 0)
    }

    @Test
    fun testDifferentEnds() {
        setGrammar(
            """
            # start and end are of different color
            root = (no1 (TERM &TERM)* "1" | no2 (TERM &TERM)* "2" | no3 (TERM &TERM)* "3" | no4 (TERM &TERM)* "4") !.

            no1 = "2" | "3" | "4"
            no2 = "1" | "3" | "4"
            no3 = "1" | "2" | "4"
            no4 = "1" | "2" | "3"

            TERM = "1" | "2" | "3" | "4"
        """.trimIndent()
        )

        assertParse("12", true, 2)
        assertParse("1", false, 0)
        assertParse("15", false, 0)
        assertParse("31234124234234124", true, 17)
        assertParse("", false, 0)
    }

    @Test
    fun testHasTwo() {
        setGrammar(
            """
            # has bright green (2)
            root = no2* "2" TERM* !.

            no2 = "1" | "3" | "4"

            TERM = "1" | "2" | "3" | "4"
        """.trimIndent()
        )

        assertParse("1", false, 0)
        assertParse("2", true, 1)
        assertParse("134133413141312344341", true, 21)
        assertParse("", false, 0)
    }

    @Test
    fun testCPLogic4() {
        setGrammar(
            """# does not have shady green and red together (1 and 4)
root = no1+ !. | no4+ !.

no1 = "2" | "3" | "4"
no4 = "1" | "2" | "3""""
        )

        pipeline.interpreter.dotExceptions = ""

        assertParse("24\n312", false, 0)
        assertParse("21", true, 2)
        assertParse("12", true, 2)
        assertParse("112312", true, 6)

        assertParse("12313143", false, 0)
        assertParse("14223", false, 0)
    }
}
