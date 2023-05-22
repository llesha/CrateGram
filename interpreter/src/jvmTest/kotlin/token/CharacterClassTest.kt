package token

import kotlin.test.Test


class CharacterClassTest {
    @Test
    fun testRanges() {
        val characterClass = CharacterClass("213\\t\\-1-2\\--r")
        val variants = characterClass.variants
        println(variants)
    }
}