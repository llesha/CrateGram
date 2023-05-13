// TODO make abstract
open class PosError(val msg: String, val position: Position) : Throwable() {
    override val message: String
        get() = "$msg at $position"
}

class LexerError(msg: String, position: Position) : PosError(msg, position)
class ParserError(msg: String, position: Position) : PosError(msg, position)