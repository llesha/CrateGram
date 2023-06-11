import kotlin.test.assertEquals
import kotlin.test.assertTrue

object TestFactory {
    fun assertParse(text: String, success: Boolean, index: Int? = null) {
        val result = parse(text)
        assertEquals(success, result[0] as Boolean)
        if (index != null)
            assertEquals(index, result[1] as Int)
    }
}
