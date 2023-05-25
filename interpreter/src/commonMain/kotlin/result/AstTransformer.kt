package result

import inPlaceFilter
import token.Token

class AstTransformer {
    fun transformNode(node: Node): Node {
        when (node.name) {
            "*", "+" -> {
                node.children.inPlaceFilter { it.name != "" }
            }
        }
        return node
    }
}
