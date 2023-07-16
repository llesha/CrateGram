window.currentGrammar = "playground"
require.config({
    paths: {
        vs: "monaco-editor/min/vs",
    },
});

function getTheme() {
    let theme = localStorage.getItem("theme")
    return theme == null ? "light" : theme;
}

function changeTheme() {
    require(['vs/editor/editor.main'], function () {
        if (localStorage.getItem("theme").includes("light")) {
            setTheme("dark")
        } else (setTheme("light"))
    })
}

function setTheme(themeName) {
    require(['vs/editor/editor.main'], function () {
        localStorage.setItem('theme', themeName);
        monaco.editor.setTheme('PEG-' + themeName);
        document.documentElement.setAttribute('data-theme', themeName);
        //document.getElementById('theme-button').innerText = themeName;
    });
}

let theme = getTheme()
let themeButton = document.getElementById("theme-button");

setTheme(theme)
themeButton.innerText = theme

function updateFontSize(size) {
    if (size == null)
        size = localStorage.getItem("fontSize") ?? 16
    document.getElementById("font-input").value = size
    require(['vs/editor/editor.main'], function () {
        window.editor.updateOptions({
            fontSize: size,
        });
        if (window.textEditor != null) {
            window.textEditor.updateOptions({
                fontSize: size,
            });
        }
    });
    localStorage.setItem('fontSize', size)
}

function updateDebounce(ms) {
    if (ms == null)
        ms = 500
    localStorage.setItem("debounce", ms)
    document.getElementById("debounce-input").value = ms
}

updateDebounce(localStorage.getItem("debounce"))

function getDebounce() {
    return localStorage.getItem("debounce") ?? 500
}

function updateAstView(hasAst) {
    localStorage.setItem("ast", hasAst)
    document.getElementById("ast-toggle").checked = hasAst

    if (!hasAst) {
        document.getElementById("ast").style.display = "none"
        document.getElementsByClassName("main-grid")[0].style.gridTemplateColumns = "100% 0%"
    } else {
        document.getElementById("ast").style.display = "block"
        document.getElementsByClassName("main-grid")[0].style.gridTemplateColumns = "70% 30%"
    }
}

updateAstView(localStorage.getItem("ast") ?? true)

export function setDotExceptions(newExceptionsText) {
    document.getElementById("dot-exceptions-input").value = newExceptionsText
    document.getElementById("dot-exceptions-input").style.width = newExceptionsText.length + "ch"
    localStorage.setItem("dotExceptions", newExceptionsText)
    window.myGrammar.setDotExceptions(newExceptionsText)
}

const forUnlock = [
    "B-1",
    "B-2",
    "B-3",
    "B-4",
    "B-5",
    "parentheses",
    "parentheses-2"
]

var dependentUnlocks = {
    "parentheses": "parentheses-2",
    "sequence": "sequence-2"
}

export function unlockDependent() {
    //console.log(Object.keys())
    for (const key of Object.keys(dependentUnlocks)) {
        var lock = document.getElementById("unlock-" + key)
        console.log(lock, key)
        if (localStorage.getItem(key + "-solved") != null && lock != null) {
            lock.remove()
        } else if (lock != null && !lock.nextElementSibling.getAttribute("descr").includes("Solve")) {
            lock.nextElementSibling.setAttribute("descr", `Solve <strong>${key}</strong> first\n\n`+lock.nextElementSibling.getAttribute("descr"))
        }
    }
}

export function unlock() {
    let solvedForUnlock = 0
    for (const level of forUnlock) {
        if (localStorage.getItem(level + "-solved") != null) {
            solvedForUnlock += 1
        }
    }

    let caterpillar = document.getElementById("cater-tasks")
    if (solvedForUnlock >= 5) {
        caterpillar.setAttribute("viewBox", "0 0 320 512")
        caterpillar.getElementsByTagName("path")[0].setAttribute("d", "M278.6 233.4c12.5 12.5 12.5 32.8 0 45.3l-160 160c-12.5 12.5-32.8 12.5-45.3 0s-12.5-32.8 0-45.3L210.7 256 73.4 118.6c-12.5-12.5-12.5-32.8 0-45.3s32.8-12.5 45.3 0l160 160z")
    }
    else {
        let clickable = caterpillar.nextElementSibling
        if (!clickable.getAttribute("descr").includes("Solve any 5 tasks from Binary"))
            clickable.setAttribute("descr", "<strong>Solve any 5 tasks from Binary and Other to unlock</strong>\n\n" + clickable.getAttribute("descr"))
    }
}

unlock()
unlockDependent()

export { setTheme, getTheme, updateFontSize, updateDebounce, getDebounce, updateAstView }
