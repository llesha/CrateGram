import { setTheme, updateFontSize, updateDebounce } from "./loader.js";
import * as utils from "./utils.js"


//#region click listeners
document.getElementById("menu-button").onclick = () => {
    let left = document.getElementsByClassName("left panel")[0]
    let menu = document.getElementById("menu")
    left.style.borderRight = left.style.width == "25%" ? "0px" : "var(--gray) solid 1px"
    left.style.width = left.style.width == "25%" ? "0%" : "25%"
    let right = document.getElementsByClassName("right panel")[0]
    right.style.width = right.style.width == "65%" ? "90%" : "65%"
    menu.classList = utils.addOrRemove(menu.classList, "hidden").toString().replaceAll(",", " ")
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
        let index = classList.indexOf("fa-angle-down")
        let next = e.nextElementSibling
        if (index == -1) {
            classList.splice(classList.indexOf("fa-angle-right"), 1)
            classList.push("fa-angle-down")
            next.style.display = "block"
        } else {
            classList.splice(index, 1)
            classList.push("fa-angle-right")
            next.style.display = "none"
        }
        e.previousElementSibling.classList = classList.join(" ")
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

grids[0].getElementsByClassName("grid-cell")[0].onclick = clearValid
grids[1].getElementsByClassName("grid-cell")[0].onclick = clearInvalid

function clearValid() {
    let saved = grids[0].getElementsByClassName("grid-cell")[0]
    grids[0].innerHTML = saved.outerHTML
    grids[0].getElementsByClassName("grid-cell")[0].onclick = clearValid
}

function clearInvalid() {
    let saved = grids[1].getElementsByClassName("grid-cell")[0]
    grids[1].innerHTML = saved.outerHTML
    grids[1].getElementsByClassName("grid-cell")[0].onclick = clearInvalid
}
//#endregion
