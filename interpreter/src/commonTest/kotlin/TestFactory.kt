import kotlin.test.assertEquals
import kotlin.test.assertTrue

object TestFactory {
    fun assertParse(text: String, success: Boolean, index: Int? = null) {
        val result = parse(text)
        assertEquals(result[0] as Boolean, success)
        if (index != null)
            assertEquals(result[1] as Int, index)
    }
}
