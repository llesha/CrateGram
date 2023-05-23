require.config({
    paths: {
        vs: "node_modules/monaco-editor/min/vs",
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
        if (window.playground != null) {
            window.playground.updateOptions({
                fontSize: size,
            });
        }
    });
    localStorage.setItem('fontSize', size)
}

function updateDebounce(ms) {
    if (ms == null)
        ms = 1000
    localStorage.setItem("debounce", ms)
    document.getElementById("debounce-input").value = ms
}

updateDebounce(localStorage.getItem("debounce"))

function getDebounce() {
    return localStorage.getItem("debounce")
}

export { setTheme, getTheme, updateFontSize, updateDebounce, getDebounce };
