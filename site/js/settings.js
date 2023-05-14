function changeTheme() {
    require(['vs/editor/editor.main'], function () {
        let theme = ["light", "dark"]
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

export { changeTheme, setTheme }