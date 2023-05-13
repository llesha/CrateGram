package token

class RuleWithName(position: IntRange, val name: String, val rule: Rule) : Token("=", position) {
    override fun toString(): String = "$name = $rule"
}

class Rule(position: IntRange, val children: MutableList<Token>) : Token("=", position) {
    override fun toString(): String = children.joinToString(separator = " ")
}