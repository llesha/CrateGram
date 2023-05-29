import kotlin.js.JsName

abstract class PosError(
    @JsName("msg") val msg: String,
    @JsName("range") val range: IntRange?,
    @JsName("position") val position: Int?
) :
    Throwable() {
// https://youtrack.jetbrains.com/issue/KT-58856/Kotlin-JS-Cannot-override-message-in-Throwable
    override val message: String?
        get() = msg + " at ${range ?: position}"
}

class LexerError(msg: String, range: IntRange? = null, position: Int? = null) : PosError(msg, range, position)
class ParserError(msg: String, range: IntRange? = null, position: Int? = null) : PosError(msg, range, position)

class InterpreterError(msg: String, range: IntRange? = null, position: Int? = null) :
    PosError(msg, range, position)
