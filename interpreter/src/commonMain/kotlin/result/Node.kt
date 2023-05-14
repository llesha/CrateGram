package result

/**
 * AST node.
 * @param name name of [token.Rule] from which node is generated
 * @param children child nodes
 */
open class Node(val name: String, val children: MutableList<Node>) {}

/**
 * Represents leaf node without children
 */
class ValueNode(name: String, val value:String): Node(name, mutableListOf())