import { setTheme, updateFontSize, updateDebounce, updateAstView, setDotExceptions } from "./loader.js";
import * as utils from "./testInputs.js"


function populateTasks() {

}

populateTasks()

function addOrRemove(array, element) {
    array = Array.from(array)
    const index = array.indexOf(element)
    if (index == -1) {
        array.push(element)
    } else {
        array.splice(index, 1)
    }

    return array
}

//#region click listeners
document.getElementById("menu-button").onclick = () => {
    let left = document.getElementsByClassName("left panel")[0]
    let menu = document.getElementById("menu")
    left.style.borderRight = left.style.width == "25%" ? "0px" : "var(--gray) solid 1px"
    left.style.width = left.style.width == "25%" ? "0%" : "25%"
    let right = document.getElementsByClassName("right panel")[0]
    right.style.width = right.style.width == "65%" ? "90%" : "65%"
    menu.classList = addOrRemove(menu.classList, "hidden").toString().replaceAll(",", " ")
}

let themeButton = document.getElementById("theme-button")
themeButton.onclick = () => {
    let changedTheme = themeButton.innerText == "light" ? "dark" : "light"
    themeButton.innerText = changedTheme
    setTheme(changedTheme)
}

let expandables = document.getElementsByClassName("expandable")

for (const e of expandables) {
    e.onclick = () => {
        let classList = Array.from(e.previousElementSibling.classList)
        let index = classList.indexOf("menu-down")
        let next = e.nextElementSibling
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
    let children = e.parentElement.getElementsByClassName("task-list")[0].children
    console.log(e.textContent)
    for (const child of children) {
        child.onclick = () => {
            console.log(child.textContent)
            fetch(`../resources/grammar/${e.textContent}/${child.textContent}.txt`)
                .then(e => e.text())
                .then(t => {
                    console.log(t)
                    window.Interpreter.setGrammar(t)
                    document.getElementById("current-task").innerText = "Check " + child.textContent
                    document.getElementById("current-task").classList.add("hoverable")
                    document.getElementById("current-task").classList.add("clickable")
                    document.getElementById("grammar-type").style.display = "block"
                    window.currentGrammar = child.textContent
                    window.editor.setValue(localStorage.getItem(window.currentGrammar) ?? " ")
                })
        }
    }
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
    window.editor.setValue(localStorage.getItem("playground"))
    document.getElementById("current-task").innerText = "Playground"
    document.getElementById("current-task").classList = []
    document.getElementById("grammar-type").style.display = "none"
    window.currentGrammar = "playground"
}

let dotExceptionsInput = document.getElementById("dot-exceptions-input")
dotExceptionsInput.oninput = () => {
    setDotExceptions(dotExceptionsInput.value)
}

dotExceptionsInput.parentElement.onclick = () => {
    dotExceptionsInput.focus()
}
//#endregion
