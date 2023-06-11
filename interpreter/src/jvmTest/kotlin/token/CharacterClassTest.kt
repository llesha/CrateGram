package token

import kotlin.test.Test


class CharacterClassTest {
    @Test
    fun testRanges() {
        val characterClass = CharacterClass("213\\t\\-1-2\\--r")
        val variants = characterClass.getVariants()
        println(variants)
    }

    @Test
    fun testEmpty() {
        val characterClass = CharacterClass("")
        val variants = characterClass.getVariants()
        println(variants)
    }
}
