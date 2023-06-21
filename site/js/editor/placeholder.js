import { addGrammarExamples } from "../index.js";
import { getDebounce } from "../loader.js";
import { addValueToTable, moveCells } from "../testInputs.js";

/**
         * Represents an placeholder renderer for monaco editor
         * https://github.com/microsoft/monaco-editor/issues/568#issuecomment-1511031193
         */
class PlaceholderContent {
    static ID = 'editor.widget.placeholderHint';

    constructor(placeholder, editor, contentChangeFunction) {
        this.placeholder = placeholder;
        this.editor = editor;
        editor.onDidChangeModelContent(() => this.onDidChangeModelContent(contentChangeFunction));
        this.onDidChangeModelContent(contentChangeFunction);
    }

    onDidChangeModelContent(contentChangeFunction) {
        if (this.editor.hasMarkers) {
            this.editor.removeMarkers()
        }
        if (this.editor.getValue() === '') {
            if (this.editor === window.textEditor)
                contentChangeFunction(this.editor)
            this.editor.addContentWidget(this);
        } else {
            this.editor.removeContentWidget(this);
            contentChangeFunction(this.editor)
        }
    }

    getId() {
        return PlaceholderContent.ID;
    }

    getDomNode() {
        if (!this.domNode) {
            this.domNode = document.createElement('div');
            this.domNode.style.width = 'max-content';
            this.domNode.style.color = "var(--gray)";
            this.domNode.textContent = this.placeholder;
            this.domNode.style.fontStyle = 'italic';
            this.domNode.style.pointerEvents = "none"
            this.editor.applyFontInfo(this.domNode);
        }

        return this.domNode;
    }

    getPosition() {
        return {
            position: { lineNumber: 1, column: 1 },
            preference: [monaco.editor.ContentWidgetPositionPreference.EXACT],
        };
    }

    dispose() {
        this.editor.removeContentWidget(this);
    }
}

var currentTextInputStatus = null
var currentTextInput = null
var taskInputStatus = null
var decorations = []
var myGrammarHasError = false

function parseTask() {
    taskInputStatus = window.Interpreter.parse(currentTextInput)
}

function parseMyGrammar() {
    if (window.myGrammar.hasGrammar()) {
        currentTextInputStatus = window.myGrammar.parse(currentTextInput)
    }
}

function getDecorationOptions() {
    if (currentTextInputStatus?.[0] == true && taskInputStatus?.[0] == true) {
        return {
            glyphMarginClassName: 'tm-letter',
            glyphMarginHoverMessage: { value: 'input matches both task and your grammar' }
        }
    } else if (currentTextInputStatus?.[0] == true) {
        return {
            glyphMarginClassName: 'm-letter',
            glyphMarginHoverMessage: { value: 'input matches your grammar' }
        }
    } else if (taskInputStatus?.[0] == true) {
        return {
            glyphMarginClassName: 't-letter',
            glyphMarginHoverMessage: { value: 'input matches task grammar' }
        }
    }

    return { ...res, isWholeLine: false }
}

export function addPlaceholdersWithOnInput() {
    function removeMarkers(editor) {
        window.editor.hasMarkers = false
        monaco.editor.setModelMarkers(editor.getModel(), "owner", [])
    }
    window.textEditor.removeMarkers = () => removeMarkers(window.textEditor)
    window.editor.removeMarkers = () => removeMarkers(window.editor)

    new PlaceholderContent('ast (generated automatically on input hover)', window.ast, () => { })

    new PlaceholderContent('input to check (Ctrl + Enter to store)',
        window.textEditor, (editor) => {
            currentTextInput = editor.getValue()
            let grammarType = document.getElementById("grammar-type").textContent
            if (grammarType == "task grammar") {
                currentTextInputStatus = null
                parseTask()
            } else if (grammarType == "my grammar") {
                taskInputStatus = null
                parseMyGrammar()
            } else {
                parseTask()
                parseMyGrammar()
            }
            // console.log(currentTextInputStatus, taskInputStatus)
            if ((taskInputStatus == null || taskInputStatus[0] != true) &&
                (currentTextInputStatus == null || currentTextInputStatus[0] != true)) {
                decorations = window.textEditor.deltaDecorations(decorations, [])
            }
            else {
                decorations = window.textEditor.deltaDecorations(decorations, [{
                    options: {
                        isWholeLine: false,
                        ...getDecorationOptions()
                    },
                    range: { startLineNumber: 1, startColumn: 1, endLineNumber: 1, endColumn: 1 }
                }])
            }
            // if (window.debounceInput != null) {
            //     clearTimeout(window.debounceInput)
            // }
            // window.debounceInput = setTimeout(() => {
            //     window.debounceInput = null;
            //     let value = editor.getValue()
            //     let result = window.Interpreter.parse(value)
            //     if (!result[0])
            //         showMarkers("", result[1], window.textEditor)
            //     addValueToTable(result[0], result[1], value)
            // }, getDebounce().toString());
        });
    new PlaceholderContent('grammar', window.editor, (editor) => {
        document.getElementById("spinner").style.display = "block"
        if (window.debounce != null) {
            clearTimeout(window.debounce)
        }
        window.debounce = setTimeout(() => {
            loadGrammar()
        }, getDebounce().toString());
    });

    window.textEditor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, function () {
        addTest()
    })
}

export function addTest() {
    if (currentTextInputStatus != null &&
        (!myGrammarHasError || document.getElementById("grammar-type") == "task grammar"))
        addValueToTable(currentTextInputStatus?.[0], taskInputStatus?.[0], currentTextInputStatus?.[1], currentTextInput)
}

export function loadGrammar() {
    document.getElementById("spinner").style.display = "none"
    window.debounce = null;
    myGrammarHasError = false;
    let value = editor.getValue()
    localStorage.setItem(window.currentGrammar, value)
    let errorElement = document.getElementById("grammar-error")
    try {
        window.myGrammar.setGrammar(value)
    } catch (error) {
        window.myGrammar.clearGrammar()
        myGrammarHasError = true
        // console.log(error)
        if (error.msg == null) {
            errorElement.innerText = `Unexpected error: ${error}`
        }
        else {
            errorElement.innerText = error.msg
            showMarkers(error.msg,
                error.position ?? {
                    first: window.Interpreter.getStarting(error.range),
                    second: window.Interpreter.getEnding(error.range)
                },
                window.editor)
        }
        errorElement.setAttribute("descr", errorElement.innerText)
        if(errorElement.innerText.length > 38) {
            errorElement.innerText = errorElement.innerText.substring(0, 38) + "..."
        }
        if (document.getElementById("grammar-type") != "task grammar")
            window.textEditor.updateOptions({
                readOnly: true,
            });
    } finally {
        if (!myGrammarHasError) {
            errorElement.innerText = ""
            window.textEditor.updateOptions({
                readOnly: false,
            });
        }
    }
    moveCells()
}

function showMarkers(msg, range, editor) {
    if (!isNaN(range))
        range = { first: range, second: range }
    let start = editor.getModel().getPositionAt(range.first)
    let end = editor.getModel().getPositionAt(range.second)
    editor.hasMarkers = true

    monaco.editor.setModelMarkers(editor.getModel(), "owner", [{
        message: msg,
        severity: monaco.MarkerSeverity.Error,
        startLineNumber: start.lineNumber,
        startColumn: start.column,
        endLineNumber: end.lineNumber,
        endColumn: end.column,
    }])
}
