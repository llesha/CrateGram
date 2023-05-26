import token.IdentToken
import token.Token

inline fun <T : Any, R> T?.ifNotNull(action: T.() -> R) = if (this != null) action(this) else null

fun List<Token>.listRange() = first().range.first..last().range.last

fun <T> List<T>.subList(start: Int) = this.subList(start, size).toMutableList()

var index = 0

/**
 * For reducing grammars we need to create new rules.
 * This function creates names for the rules
 */
fun generateNonTerminalName(names: MutableSet<IdentToken>): IdentToken {
    var res: String
    val nameStrings = names.map { it.symbol }
    while ("$${index}".also { res = it } in nameStrings) {
        index++
    }
    val result = IdentToken(res)
    names.add(result)
    return result
}

fun <T> MutableList<T>.inPlaceFilter(condition: (T) -> Boolean): MutableList<T> {
    val newElements = this.filter(condition)
    clear()
    addAll(newElements)
    return this
}
