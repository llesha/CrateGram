package token

class RuleWithName(position: IntRange, val name: String, val rule: Rule) : Token("=", position) {
    override fun toString(): String = "$name = $rule"
}

class Rule(position: IntRange, var child: Token) : Token("=", position) {
    override fun toString(): String = child.toString()
}