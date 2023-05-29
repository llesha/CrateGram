package result

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Represents leaf node without children
 */
class ValueNode(name: String) : Node(name, mutableListOf())

/**
 * AST node.
 * @param name name of [token.Rule] from which node is generated
 * @param children child nodes
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("AstNode")
open class Node(
    @JsName("name") val name: String,
    @JsName("children") val children: MutableList<Node> = mutableListOf()
) {
    override fun toString(): String = name

    @JsName("toJson")
    fun toJson(offset: Int = 0): String {
        if (this is ValueNode)
            return "\"$name\""
        if (children.size == 1) {
            return """${" ".repeat(offset)}{
  ${" ".repeat(offset)}"$name": ${children.first().toJson(offset + 2)}
${" ".repeat(offset)}}""".trimMargin()
        }
//        if (children.isEmpty())
//            throw InterpreterError("Unexpected empty node")
        return """${" ".repeat(offset)}{
  ${" ".repeat(offset)}"$name": [
    ${" ".repeat(offset)}${children.joinToString(separator = ", ") { it.toJson() }}
    ${" ".repeat(offset)}]
${" ".repeat(offset)}}""".trimMargin()
    }
}
