import { getDebounce } from "../loader.js";
import { addValueToTable } from "../testInputs.js";

/**
         * Represents an placeholder renderer for monaco editor
         * https://github.com/microsoft/monaco-editor/issues/568#issuecomment-1511031193
         */
class PlaceholderContent {
    static ID = 'editor.widget.placeholderHint';

    constructor(placeholder, editor, contentChangeFunction) {
        this.placeholder = placeholder;
        this.editor = editor;
        // register a listener for editor code changes
        editor.onDidChangeModelContent(() => this.onDidChangeModelContent(contentChangeFunction));
        // ensure that on initial load the placeholder is shown
        this.onDidChangeModelContent(contentChangeFunction);
    }

    onDidChangeModelContent(contentChangeFunction) {
        if (this.editor === window.textEditor) {
            let grammarType = document.getElementById("grammar-type")
            if (grammarType == "task grammar") {

            }
            else if (window.myGrammar.hasGrammar()) {
                currentTextInput = this.editor.getValue()
                currentTextInputStatus = window.myGrammar.parse(currentTextInput)
                if (currentTextInputStatus[0]) {
                    decorations = window.textEditor.deltaDecorations(decorations, [{
                        options: {
                            isWholeLine: false,
                            glyphMarginClassName: 'fa-solid tb-letter',
                            glyphMarginHoverMessage: { value: 'glyph margin hover message' }
                        },
                        range: { startLineNumber: 1, startColumn: 1, endLineNumber: 1, endColumn: 1 }
                    }])
                }
                else {
                    decorations = window.textEditor.deltaDecorations(decorations, [])
                }
            }
        }
        if (this.editor.hasMarkers) {
            this.editor.removeMarkers()
        }
        if (this.editor.getValue() === '') {
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
            this.domNode.style.color = "#929292";
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

export function addPlaceholdersWithOnInput() {
    // TODO: remove duplicate code
    window.editor.removeMarkers = function () {
        window.editor.hasMarkers = false
        monaco.editor.setModelMarkers(window.editor.getModel(), "owner", [])
    }

    window.textEditor.removeMarkers = function () {
        window.textEditor.hasMarkers = false
        monaco.editor.setModelMarkers(window.textEditor.getModel(), "owner", [])
    }

    let showMarkers = function (msg, range, editor) {
        if (!isNaN(range)) {
            range = { first: range, second: range }
        }
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

    new PlaceholderContent('input to check (press Enter to store, Shift + Enter for line break)',
        window.textEditor, (editor) => {
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
            document.getElementById("spinner").style.display = "none"
            window.debounce = null;
            myGrammarHasError = false;
            let value = editor.getValue()
            localStorage.setItem(window.currentGrammar, value)
            let errorElement = document.getElementById("grammar-error")
            try {
                window.myGrammar.setGrammar(value)
            } catch (error) {
                myGrammarHasError = true
                if (error.msg == null)
                    errorElement.innerText = `Unexpected error: ${error}`
                else {
                    errorElement.innerText = error.msg
                    console.log(error)
                    showMarkers(error.msg,
                        //TODO: change range bad names of fields
                        error.position ?? {
                            first: error.range.v1_1 - 1,
                            second: error.range.w1_1 - 1
                        },
                        window.editor)
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
        }, getDebounce().toString());
    });

    window.textEditor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, function () {
        if (currentTextInputStatus != null &&
            (!myGrammarHasError || document.getElementById("grammar-type") == "task grammar"))
            addValueToTable(currentTextInputStatus[0], currentTextInputStatus[1], currentTextInput)
    })
}