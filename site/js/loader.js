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
}

setDotExceptions(localStorage.getItem("dotExceptions") ?? "\n\r")

export { setTheme, getTheme, updateFontSize, updateDebounce, getDebounce, updateAstView };
