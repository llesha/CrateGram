import reductions.*
import result.Pipeline
import kotlin.test.Ignore
import kotlin.test.Test

class RuleTransformerTest {
    @Ignore
    @Test
    fun testRemovePredicates() {
        val pipeline = Pipeline().setGrammar(
            """# does not have shady green and red together (1 and 4)
root = no1+ !. | no4+ !.

no1 = "2" | "3" | "4"
no4 = "1" | "2" | "3"""".trimIndent()
        )

        transformRules(pipeline.interpreter.rules)
        println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
        println()

        stage1(pipeline.interpreter.rules)
        println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
        println()

        stage2(pipeline.interpreter.rules)
        println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
        println()

        stage3(pipeline.interpreter.rules)
        println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
    }

    @Test
    fun simpleTest() {
        val pipeline = Pipeline().setGrammar(
            """root = A | B
               A = "1"
               B = "2"
            """.trimIndent()
        )

        transformRules(pipeline.interpreter.rules)
        val ruleTokens = pipeline.interpreter.rules.values.toMutableList()
        for (i in ruleTokens.indices) {
            val tokenRule = ruleTokens[i].child
            ruleTokens[i].child = expressionsToTwoChildren(tokenRule)
        }

       // addNewRules(pipeline.interpreter.rules)
        println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
        println()

        stage1(pipeline.interpreter.rules)
        println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
        println()

        stage2(pipeline.interpreter.rules)
        println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
        println()

        addNewRules(pipeline.interpreter.rules)
        println(pipeline.parse("1").joinToString(separator = ", "))
        println(pipeline.parse("2").joinToString(separator = ", "))

      //  stage3(pipeline.interpreter.rules)
       // println(pipeline.interpreter.rules.toList().joinToString(separator = "\n"))
    }
}
