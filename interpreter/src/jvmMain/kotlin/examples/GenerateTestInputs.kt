package examples

import random
import result.Pipeline
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

const val invalidSymbols = "!@#$%^&*"
const val BLOCK_DELIMITER = "█████████████████████████████\n"
const val VALUE_DELIMITER = "░\n"

val grammarPath: Path = Paths.get("").toAbsolutePath().parent.resolve("site/resources/grammar")
val testPath: Path = grammarPath.toAbsolutePath().parent.resolve("test")
val alphabets: Map<String, Any> = mapOf(
    "Binary" to "01",
    "Caterpillar logic" to "1234",
    "Other" to mapOf("parentheses" to "()", "parentheses-2" to "()[]<>{}")
)

/**
 * @return generated sequence and true if invalid symbols were used
 */
fun generateSequence(validSymbols: String, invalidSymbols: String): Pair<String, Boolean> {
    val length = random.nextInt(1, 100)
    val valid = random.nextInt(0, 100) < 95
    val res = StringBuilder("")
    val possibleSymbols = if (valid) validSymbols else validSymbols + invalidSymbols
    repeat(length) {
        res.append(possibleSymbols.random(random))
    }

    return res.toString() to (invalidSymbols.any { res.contains(it) })
}

fun writeTestCases(alphabets: Map<String, String>, folder: String) {
    createTestFolder(folder)
    val grammars = grammarPath.resolve(folder).listDirectoryEntries()
    val pipeline = Pipeline()
    for (grammarPath in grammars) {
        println(grammarPath)
        pipeline.setGrammar(grammarPath.toFile().readText())
        val grammarName = grammarPath.fileName.toString().removeSuffix(".txt")
        val testFile = testPath.resolve(folder)
            .resolve("${grammarName}-test.txt")
            .toFile()
        testFile.writeText("")

        val (validInputs, invalidInputs, invalidWithInvalidSymbols) = createTestCases(
            alphabets[grammarName] ?: alphabets["*"]!!, pipeline
        )
        validInputs.toList().sortedBy { it.length }.forEach { testFile.appendText("$it$VALUE_DELIMITER") }
        testFile.appendText(BLOCK_DELIMITER)

        invalidInputs.toList().sortedBy { it.length }.forEach { testFile.appendText("$it$VALUE_DELIMITER") }
        testFile.appendText(BLOCK_DELIMITER)

        invalidWithInvalidSymbols.forEach { testFile.appendText("$it$VALUE_DELIMITER") }
    }
}

private fun createTestCases(
    alphabet: String,
    pipeline: Pipeline
): Triple<MutableSet<String>, MutableSet<String>, MutableSet<String>> {
    val validInputs = mutableSetOf<String>()
    val invalidInputs = mutableSetOf<String>()
    val invalidWithInvalidSymbols = mutableSetOf<String>()
    repeat(300) {
        val (input, hasInvalidSymbols) = generateSequence(alphabet, invalidSymbols)
        val (result, _) = pipeline.parse(input)
        if (result as Boolean) {
            if (hasInvalidSymbols)
                throw Exception("Invalid symbols input (in grammar $grammarPath) is correct: `$input`")
            validInputs.add(input)
        } else {
            if (hasInvalidSymbols)
                invalidWithInvalidSymbols.add(input)
            else
                invalidInputs.add(input)
        }
    }
    return Triple(validInputs, invalidInputs, invalidWithInvalidSymbols)
}

private fun createTestFolder(folderName: String) {
    val path = testPath.resolve(folderName)
    if (!path.exists()) {
        path.toFile().mkdir()
    }
}

fun main() {
    for ((folder, alphabet) in alphabets) {
        println(folder)
        writeTestCases(if (alphabet is String) mapOf("*" to alphabet) else alphabet as Map<String, String>, folder)
    }
}
