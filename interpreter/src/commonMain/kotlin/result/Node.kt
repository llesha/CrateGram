package result

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
@JsName("Node")
open class Node(
    @JsName("name") val name: String,
    @JsName("children") val children: MutableList<Node> = mutableListOf()
) {
    override fun toString(): String = name
}
