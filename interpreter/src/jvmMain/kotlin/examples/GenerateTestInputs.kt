package examples

import random
import result.Pipeline
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

const val invalidSymbols = "!@#$%^&*()05 "
const val BLOCK_DELIMITER = "█████████████████████████████\n"
const val VALUE_DELIMITER = "░\n"

val grammarPath: Path = Paths.get("").toAbsolutePath().parent.resolve("site/resources/grammar")
val testPath: Path = grammarPath.toAbsolutePath().parent.resolve("test")

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

fun createCaterpillarLogicTestCases() {
    val alphabet = "1234"
    val cpLogicFolder = "Caterpillar logic"
    createTestFolder(cpLogicFolder)
    val grammars = grammarPath.resolve(cpLogicFolder).listDirectoryEntries()
    val pipeline = Pipeline()
    for (grammarPath in grammars) {
        pipeline.setGrammar(grammarPath.toFile().readText())

        val testFile = testPath.resolve(cpLogicFolder)
            .resolve("${grammarPath.fileName.toString().removeSuffix(".txt")}-test.txt")
            .toFile()
        testFile.writeText("")
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
        validInputs.toList().sortedBy { it.length }.forEach { testFile.appendText("$it$VALUE_DELIMITER") }
        testFile.appendText(BLOCK_DELIMITER)

        invalidInputs.toList().sortedBy { it.length }.forEach { testFile.appendText("$it$VALUE_DELIMITER") }
        testFile.appendText(BLOCK_DELIMITER)

        invalidWithInvalidSymbols.forEach { testFile.appendText("$it$VALUE_DELIMITER") }
    }
}

private fun createTestFolder(folderName:String) {
    val path = testPath.resolve(folderName)
    if(!path.exists()) {
        path.toFile().mkdir()
    }
}

fun main() {
    createCaterpillarLogicTestCases()
}
