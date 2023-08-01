import { addTest, loadGrammar } from "./editor/placeholder.js";
import { setTheme, updateFontSize, updateDebounce, updateAstView, setDotExceptions, unlock, unlockDependent } from "./loader.js";
import * as utils from "./testInputs.js"

const BLOCK_DELIMITER = "█████████████████████████████\n"
const VALUE_DELIMITER = "░\n"
const ANTI_CHEAT = "3"

//#region click listeners
document.getElementById("menu-button").onclick = () => {
    let left = document.getElementsByClassName("left")[0]
    let menu = document.getElementById("menu")
    left.style.borderRight = left.style.width == "25%" ? "0px" : "var(--gray) solid 1px"
    left.style.width = left.style.width == "25%" ? "5%" : "25%"
    let right = document.getElementsByClassName("right panel")[0]
    right.style.width = right.style.width == "65%" ? "90%" : "65%"
    menu.classList = _addOrRemove(menu.classList, "hidden").toString().replaceAll(",", " ")
    left.classList = _addOrRemove(left.classList, "no-scroll").toString().replaceAll(",", " ")
}

function _addOrRemove(array, element) {
    array = Array.from(array)
    const index = array.indexOf(element)
    if (index == -1) {
        array.push(element)
    } else {
        array.splice(index, 1)
    }

    return array
}

let themeButton = document.getElementById("theme-button")
themeButton.onclick = () => {
    let changedTheme = themeButton.innerText == "light" ? "dark" : "light"
    themeButton.innerText = changedTheme
    setTheme(changedTheme)
}

let expandables = document.getElementsByClassName("expandable")
let currentTask = document.getElementById("current-task")

for (let e of expandables) {
    // check that corresponding svg is not `lock`
    if (e.previousElementSibling.getAttribute("viewBox") != "0 0 448 512")
        e.onclick = () => {
            let classList = Array.from(e.previousElementSibling.classList)
            let index = classList.indexOf("menu-down")
            let next = e.parentElement.nextElementSibling
            if (index == -1) {
                classList.splice(classList.indexOf("menu-right"), 1)
                classList.push("menu-down")
                next.style.display = "block"
            } else {
                classList.splice(index, 1)
                classList.push("menu-right")
                next.style.display = "none"
            }
            e.previousElementSibling.classList = classList.join(" ")
        }

}

function setCompleteTasks() {
    for (let e of expandables) {
        let children = e.parentElement.nextElementSibling.children
        if (e.textContent != "Grammar tasks")
            for (let child of children) {
                if (localStorage.getItem(child.textContent.trim() + "-solved") != null) {
                    child.classList.add("complete")
                }
                child.onclick = () => {
                    if (child.getElementsByTagName("svg").length == 0)
                        _setTask(e, child)
                }
            }
    }
}

setCompleteTasks()

function _setTask(e, child) {
    fetch(`..${_getSite()}/resources/grammar/${e.textContent}/${child.textContent.trim()}.txt`)
        .then(f => f.text())
        .then(text => {
            window.firstTime = true
            utils.clearValid()
            utils.clearInvalid()
            document.getElementById("grammar-type").textContent = "task grammar"
            // console.log(text)
            window.Interpreter.setGrammar(text)
            currentTask.innerText = "Check " + child.textContent.trim()
            currentTask.classList.add("hoverable")
            currentTask.classList.add("clickable")
            document.getElementById("grammar-type").style.display = "block"
            window.currentGrammar = child.textContent.trim()
            window.currentGrammarBlock = e.textContent
            window.editor.setValue(localStorage.getItem(window.currentGrammar) ?? " ")
            if (localStorage.getItem(child.textContent.trim() + "-solved") != null) {
                _addSolvedSpan()
            }
            else {
                document.getElementById("solved-text").style.display = "none"
            }

            loadGrammar()
            if (window.currentGrammar != "playground" && window.firstTime) {
                delete window.firstTime
                if (window.myGrammar.hasGrammar())
                    document.getElementById("grammar-type").textContent = "both grammars"
                else
                    document.getElementById("grammar-type").textContent = "task grammar"
                addGrammarExamples()
            }
        })

}

function processGrammarTests(f) {
    fetch(`..${_getSite()}/resources/test/${window.currentGrammarBlock}/${window.currentGrammar}-test.txt`)
        .then(f => f.text())
        .then(text => {
            f(text)
        })
}

function _getSite() {
    return localStorage.getItem("isLocalhost") == "true" ? "" : "/CrateGram"
}

export function addGrammarExamples() {
    processGrammarTests(text => {
        let blocks = text.split(BLOCK_DELIMITER)
        let valid = blocks[0].split(VALUE_DELIMITER).splice(0, 5)
        for (const validElement of valid) {
            if (window?.myGrammar?.hasGrammar())
                utils.addValueToTable(window.myGrammar.parse(validElement)[0], true, 0, validElement)
            else
                utils.addValueToTable(null, true, 0, validElement)
        }
        let invalidWithValidDictionary = blocks[1].split(VALUE_DELIMITER).splice(0, 5)
        for (const invalidElement of invalidWithValidDictionary) {
            if (window?.myGrammar?.hasGrammar())
                utils.addValueToTable(window.myGrammar.parse(invalidElement)[0], false, 0, invalidElement)
            else
                utils.addValueToTable(null, false, 0, invalidElement)
        }
    })
}

let fontInput = document.getElementById("font-input")
fontInput.onchange = () => {
    let normalized = Math.min(Math.max(fontInput.value, 5), 80)
    updateFontSize(normalized)
}

let debounceInput = document.getElementById("debounce-input")
debounceInput.onchange = () => {
    let normalized = Math.min(Math.max(debounceInput.value, 100), 10000)
    updateDebounce(normalized)
}

let grids = document.getElementsByClassName("status-grid")

grids[0].getElementsByClassName("grid-cell")[0].onclick = utils.clearValid
grids[1].getElementsByClassName("grid-cell")[0].onclick = utils.clearInvalid

document.getElementById("ast-toggle").oninput = (e) => {
    updateAstView(e.target.checked)
}

let grammarType = document.getElementById("grammar-type")
grammarType.onclick = () => {
    if (grammarType.innerText == "my grammar") {
        grammarType.innerText = "task grammar"
    } else if (grammarType.innerText == "task grammar") {
        grammarType.innerText = "both grammars"
    } else {
        grammarType.innerText = "my grammar"
    }
}

const descrElements = document.getElementsByClassName("descr")
const descr = document.getElementById("descr")
for (const e of descrElements) {
    e.onmouseenter = (event) => {
        descr.style.left = (event.pageX + 10) + "px"
        descr.style.top = (event.pageY) + "px"
        descr.style.display = "block"
        descr.innerHTML = e.getAttribute("descr")
    }
    e.onmouseleave = () => {
        descr.style.display = "none"
    }
}

descr.onmouseenter = () => {
    descr.style.display = "block"
}

descr.onmouseleave = () => {
    descr.style.display = "none"
}

document.getElementById("playground").onclick = () => {
    document.getElementById("solved-text").style.display = "none"
    window.Interpreter.clearGrammar()
    window.editor.setValue(localStorage.getItem("playground"))
    currentTask.innerText = "Playground"
    currentTask.classList = []
    document.getElementById("error-test-text").style.display = "none"
    document.getElementById("grammar-type").style.display = "none"
    document.getElementById("grammar-type").textContent = "my grammar"
    window.currentGrammar = "playground"
    utils.clearValid()
    utils.clearInvalid()
}

let dotExceptionsInput = document.getElementById("dot-exceptions-input")
dotExceptionsInput.oninput = () => {
    setDotExceptions(dotExceptionsInput.value)
}

dotExceptionsInput.parentElement.onclick = () => {
    dotExceptionsInput.focus()
}

currentTask.onclick = () => {
    if (currentTask.textContent == "Playground")
        return
    processGrammarTests(text => {
        let blocks = text.split(BLOCK_DELIMITER)
        let valid = blocks[0].split(VALUE_DELIMITER)
        let invalid = blocks[1].split(VALUE_DELIMITER)
        let invalidWithInvalidDictionary = blocks[2].split(VALUE_DELIMITER)

        // removing last '' elements which are created because of split
        valid.pop()
        invalid.pop()
        invalidWithInvalidDictionary.pop()

        invalid.concat(invalidWithInvalidDictionary)
        let iter = 0;
        for (const validText of valid) {
            let result = window.myGrammar.parse(validText)[0]
            if (!result) {
                if (iter + ANTI_CHEAT >= valid.length) {
                    document.getElementById("error-test-text").textContent = `WA on one of hidden ${ANTI_CHEAT} tests`
                } else
                    document.getElementById("error-test-text").textContent = `WA: '${validText}'`
                return
            }
        }
        for (const invalidText of invalid) {
            let result = window.myGrammar.parse(invalidText)[0]
            if (result) {
                document.getElementById("error-test-text").textContent = `WA: '${invalidText}'`
                return
            }
        }
        localStorage.setItem(window.currentGrammar + "-solved", window.editor.getValue())
        document.getElementById("error-test-text").textContent = ""
        _addSolvedSpan()
        setCompleteTasks()
        unlock()
        unlockDependent()
    })
}

document.getElementById("help").onclick = () => hideOrShowHelp()
document.getElementById("help-text").getElementsByTagName("button")[0].onclick = () => hideOrShowHelp()

function hideOrShowHelp() {
    let popup = document.getElementById("help-text")
    popup.classList = _addOrRemove(popup.classList, "hidden").toString().replaceAll(",", " ")
}

let helpGrammars = document.getElementsByClassName("grammar-display")
for (const grammar of helpGrammars) {
    grammar.onclick = (event) => {
        let lines = grammar.textContent.split("\n")
        lines = lines.map(line => line.trim())
        lines.splice(0, 1)
        navigator.clipboard.writeText(lines.join("\n")
            .replaceAll(new RegExp(String.fromCharCode(160), "g"), " "))

        let notification = document.getElementById("copy-notification")
        notification.style.left = (event.pageX + 10) + "px"
        notification.style.top = (event.pageY) + "px"
        notification.style.display = "block"
        setTimeout(() => {
            notification.style.display = "none"
        }, 1000)
    }
}

document.getElementById("add-input").onclick = () => addTest()

//#endregion

function _addSolvedSpan() {
    let solved = document.getElementById("solved-text")
    solved.style.display = "inherit"
    solved.setAttribute("descr", localStorage.getItem(window.currentGrammar + "-solved"))
}

// setTimeout(function() {
//     console.log(window.editor)
//     const html = window.editor.viewModel.getHTMLToCopy([editor.getModel().getFullModelRange()], false);
//     console.log(html);
// }, 10000);