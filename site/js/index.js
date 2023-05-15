import { changeTheme, setTheme } from "./settings.js";

require.config({
    paths: {
        vs: "node_modules/monaco-editor/min/vs",
    },
});

if (localStorage.getItem("theme") == null) {
    setTheme("light")
} else {
    setTheme(localStorage.getItem("theme"))
}
