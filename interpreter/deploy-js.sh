#!/bin/bash

# to create gradlew, run `gradle wrapper --gradle-version 7.4.2`
# version can be found in /gradle/wrapper/*.properties - here look at distribution URL
# to execute it with intellij, copy command in embedded terminal and press Ctrl+Enter
./gradlew jsBrowserDistribution

compiled=$(<..\\site\\js\\interpreter\\Interpreter.js)
# replace `module.exports` to `window` at the end of the file
echo $compiled | sed 's/module.exports.Interpreter=/window.Interpreter=/' > ..\\site\\js\\interpreter\\Interpreter.js
