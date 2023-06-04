package examples

import random
import result.Pipeline
import java.nio.file.Paths
import kotlin.io.path.listDirectoryEntries

const val invalidSymbols = "!@#$%^&*()05 "
const val delimiter = "█████████████████████████████\n"
const val valueDelimiter = "░\n"

val grammarPath = Paths.get("").toAbsolutePath().parent.resolve("site/resources/grammar")
val testPath = grammarPath.toAbsolutePath().parent.resolve("test")

fun generateSequence(validSymbols: String, invalidSymbols: String): String {
    val length = random.nextInt(1, 100)
    val valid = random.nextInt(0, 100) < 95
    val res = StringBuilder("")
    val possibleSymbols = if (valid) validSymbols else validSymbols + invalidSymbols
    repeat(length) {
        res.append(possibleSymbols.random(random))
    }

    return res.toString()
}

fun createCpLogicTestCases() {
    val valid = "1234"
    val cpLogicFolder = "CP-logic"
    val grammars = grammarPath.resolve(cpLogicFolder).listDirectoryEntries()
    val pipeline = Pipeline()
    for (grammarPath in grammars) {
        pipeline.setGrammar(grammarPath.toFile().readText())

        val testFile = testPath.resolve(cpLogicFolder)
            .resolve("${grammarPath.fileName.toString().removeSuffix(".txt")}-test.txt")
            .toFile()
        testFile.writeText("")
        val invalidInputs = mutableListOf<String>()
        repeat(300) {
            val input = generateSequence(valid, invalidSymbols)
            val (result, _) = pipeline.parse(input)
            if (result as Boolean) {
                testFile.appendText("$input$valueDelimiter")
            } else invalidInputs.add(input)
        }
        testFile.appendText(delimiter)
        invalidInputs.forEach { testFile.appendText("$it$valueDelimiter") }
    }
}

fun main() {
    createCpLogicTestCases()
}
