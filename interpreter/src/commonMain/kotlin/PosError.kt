abstract class PosError(val msg: String, val range: IntRange?, val position: Int?) : Throwable() {
    override val message: String
        get() = "$msg at $position"
}

class LexerError(msg: String, range: IntRange? = null, position: Int? = null) : PosError(msg, range, position)
class ParserError(msg: String, range: IntRange? = null, position: Int? = null) : PosError(msg, range, position)

class InterpreterError(msg: String, range: IntRange? = null, position: Int? = null) :
    PosError(msg, range, position)