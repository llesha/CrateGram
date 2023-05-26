import result.Node
import result.Pipeline
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

val pipeline = Pipeline()

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
