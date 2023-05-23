#!/bin/bash

cd ..
./gradlew jsBrowserDistribution

#compiled=$(<..\\..\\site\\js\\interpreter\\Interpreter.js)

# very bad way to replace `module.exports` to `window` at the end of the file
#echo ${compiled::-73}"window.Interpreter=r})();
#//# sourceMappingURL=Interpreter.js.map" > ..\\..\\site\\js\\interpreter\\Interpreter.js
