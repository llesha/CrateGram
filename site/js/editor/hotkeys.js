import { clearInvalid, clearValid } from "../testInputs.js"

export function addHotkeys() {
    // remove completion showing
    window.editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Space, function () { })

    window.textEditor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Delete, function () {
        clearValid()
        clearInvalid()
    })

    window.editor.addCommand(monaco.KeyMod.Alt | monaco.KeyMod.Shift | monaco.KeyCode.KeyF, function () {
        let tokens = monaco.editor.tokenize(window.editor.getValue(), "PEG")
        let lineIndices = []
        for (const line of tokens) {
            let equal = line.filter(x => x.type == "operator.equal.PEG")
            if (equal.length != 0) {
                lineIndices.push(equal[0].offset)
            } else lineIndices.push(null)
        }
    });
}