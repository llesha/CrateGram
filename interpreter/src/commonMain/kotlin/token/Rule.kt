package token

class Rule(var child: Token) : Token("=", -1..-1) {
    override fun toString(): String = (child as? Group)?.toStringMindingParentheses(true) ?: child.toString()
}
