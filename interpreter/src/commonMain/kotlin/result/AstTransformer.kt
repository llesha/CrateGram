package result

import inPlaceFilter
import token.Token

class AstTransformer(val ast: Node) {
    fun transformAst() {
        transformNode(ast)
    }

    private fun transformNode(node: Node): Node {
        val children = node.children.toList()
//        if (node.children.size > 1)
//            node.children.inPlaceFilter { it !is ValueNode && !it.name.startsWith("__") }
        when (node.name) {
            "*", "+" -> {
                node.children.inPlaceFilter { it.name != "" }
            }
        }
        node.children.forEach { transformNode(it) }
        node.children.inPlaceFilter { it.children.isNotEmpty() || it is ValueNode }
        return node
    }
}
