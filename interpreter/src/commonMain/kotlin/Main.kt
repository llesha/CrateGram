import result.Node
import result.Pipeline
import token.AnyToken
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.random.Random

val pipeline = Pipeline()

/**
 * List of characters not recognized by [AnyToken]
 */
const val DOT_EXCEPTIONS = "\n\r\u2028\u2029"
val random = Random

@OptIn(ExperimentalJsExport::class)
@JsExport
fun setGrammar(text: String): Pipeline {
    return pipeline.setGrammar(text)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun parse(text: String): Array<Any> {
    return pipeline.parse(text)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAst(): Node {
    return pipeline.getAst()
}
