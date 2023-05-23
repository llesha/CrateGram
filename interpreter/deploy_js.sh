#!/bin/bash

# to create gradlew, run `gradle wrapper --gradle-version 7.4.2`
# version can be found in /gradle/wrapper/*.properties - here look at distribution URL
# to execute it with intellij, copy command in embedded terminal and press Ctrl+Enter
./gradlew jsBrowserDistribution

compiled=$(<..\\site\\js\\interpreter\\Interpreter.js)

# very bad way to replace `module.exports` to `window` at the end of the file
# TODO: make this symbol independent
echo ${compiled::-73}"window.Interpreter=r})();
//# sourceMappingURL=Interpreter.js.map" > ..\\site\\js\\interpreter\\Interpreter.js
