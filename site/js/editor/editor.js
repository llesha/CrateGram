import * as themes from "./themes.js";
import { config, hoverHints, tokenizer } from "./editorConstants.js";
import { getTheme, updateFontSize, getDebounce } from "../loader.js"
import { addValueToTable } from "../utils.js";


function init() {
    require(['vs/editor/editor.main'], function () {
        monaco.languages.register({ id: "PEG" });
        monaco.languages.setLanguageConfiguration("PEG", config);

        monaco.languages.setMonarchTokensProvider("PEG", {
            keywords: [
                "root",
            ],
            operators: [
                "=", "<-",

                "!", "&",

                "*", "+", "?",
                "-", ".", // regex

                "|", "/",
            ],
            // we include these common regular expressions
            symbols: /[=><!?:&|+\-*\/%]+/,
            // C# style strings
            escapes:
                /\\(?:[abfnrtv\\"']|x[\dA-Fa-f]{1,4}|u[\dA-Fa-f]{4}|U[\dA-Fa-f]{8})/,
            tokenizer: tokenizer
        });

        monaco.editor.defineTheme("PEG-light", themes.light);
        monaco.editor.defineTheme("PEG-dark", themes.dracula);

        let storedGrammar = localStorage.getItem("grammar")
        let editorOptions = {
            value: ``,
            glyphMargin: true,
            fontFamily: "Fira Code",
            fontLigatures: true,
            theme: `PEG-${getTheme()}`,
            fontSize: 16,
            automaticLayout: true,
            minimap: {
                enabled: false,
            },
            folding: false,
            lineNumbers: "off",
            lineDecorationsWidth: 0,
            lineNumbersMinChars: 0,
            bracketColorizationOptions: { enabled: false }
        }

        window.editor = monaco.editor.create(document.getElementById("editor"), {
            ...editorOptions,
            value: storedGrammar != null ? storedGrammar : `# Hello world
root =   welcome COMMA SPACE* subject punctuation !.

welcome = ("Hello" | "Greetings" | "Salute") SPACE*
subject = ("World" | "Grammar" | "PEG") SPACE*
punctuation = [!?.] SPACE*

COMMA = ","
SPACE = " "`,
            language: "PEG"
        });

        window.playground = monaco.editor.create(document.getElementById("playground"), {
            ...editorOptions
        });

        window.ast = monaco.editor.create(document.getElementById("ast"), {
            ...editorOptions,
            folding: true
        });

        /**
         * Represents an placeholder renderer for monaco editor
         * https://github.com/microsoft/monaco-editor/issues/568#issuecomment-1511031193
         */
        class PlaceholderContentWidget {
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
                return PlaceholderContentWidget.ID;
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

        new PlaceholderContentWidget('input to check', window.playground,
            (editor) => {
                if (window.debounceInput != null) {
                    clearTimeout(window.debounceInput)
                }
                window.debounceInput = setTimeout(() => {
                    window.debounceInput = null;
                    let value = editor.getValue()
                    let result = window.Interpreter.parse(value)
                    if (!result[0])
                        showMarkers("", result[1], window.playground)
                    addValueToTable(result[0], result[1], value)
                }, getDebounce().toString());
            });
        new PlaceholderContentWidget('grammar', window.editor,
            (editor) => {
                if (window.debounce != null) {
                    clearTimeout(window.debounce)
                }
                window.debounce = setTimeout(() => {
                    window.debounce = null;
                    var hadError = false
                    let value = editor.getValue()
                    localStorage.setItem("grammar", value)
                    let errorElement = document.getElementById("grammar-error")
                    try {
                        window.Interpreter.setGrammar(value)
                    } catch (error) {
                        hadError = true
                        if (error.msg == null)
                            errorElement.innerText = `Unexpected error: ${error}`
                        else {
                            errorElement.innerText = error.msg
                            showMarkers(error.msg,
                                //TODO: change range bad names of fields
                                error.position ?? {
                                    first: error.range.v1_1 - 1,
                                    second: error.range.w1_1 - 1
                                },
                                window.editor)
                        }
                    } finally {
                        if (!hadError)
                            errorElement.innerText = ""
                    }
                }, getDebounce().toString());
            });

        updateFontSize()

        // var bp = window.playground.deltaDecorations([bp], [])
        // window.playground.deltaDecorations([],
        //     [
        //         // {
        //         //     options: { isWholeLine: true, glyphMarginClassName: 'fa-solid fa-check' },
        //         //     range: { startLineNumber: 1, startColumn: 1, endLineNumber: 1, endColumn: 1 }
        //         // },
        //         {
        //             options: { isWholeLine: false, glyphMarginClassName: 'fa-solid fa-check' },
        //             range: { startLineNumber: 1, startColumn: 1, endLineNumber: 1, endColumn: 1 }
        //         }])

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

        window.editor.updateOptions({
            bracketColorizationOptions: { enabled: false },
            // fontSize: 5
        })


        monaco.languages.registerHoverProvider("PEG", {
            provideHover: function (model, position) {
                // let tokens = model.getLineTokens(position.lineNumber);
                // let currentTokenInfo = tokens._tokens[2 * tokens.findTokenIndexAtOffset(position.column) + 1];
                let index = model.getOffsetAt(position);
                let text = window.editor.getValue()
                let value = text.substring(index, index + 1)

                if ((value == '-' && index > 0 && text[index - 1] == '<')
                    || (value == '<') && index < text.length && text[index + 1] == "-") {
                    value = "<-"
                }
                let cont = hoverHints[value]
                function getRangeArgs() {
                    if (index > 0 && index < text.length && text[index + 1] !== undefined) {
                        if (text[index - 1] > text[index + 1]) {
                            return `Invalid <strong>range</strong>, left character<br> 
                            should have smaller code.
                             <br>Swap range characters.`
                        }
                        return cont + `<code>${text[index - 1]}</code> (code: ${text.charCodeAt(index - 1)}) 
                        and <code>${text[index + 1]}</code> (code: ${text.charCodeAt(index + 1)})`
                    }
                    return `<strong>Range</strong> out of bounds,<br>
                     put between two characters<br>
                     inside the <strong>Character Class</strong>`
                }
                if (cont == null && value.match(/[^\w\s\[\]()?*+]/)) {
                    cont = `<strong>Invalid</strong> - this token will not <br>
                    be tokenized, remove it`
                }
                if (cont != null)
                    return {
                        range: new monaco.Range(position.lineNumber, position.column,
                            position.lineNumber, position.column),
                        contents: [{
                            isTrusted: true,
                            supportHtml: true,
                            value: value == "-" ? getRangeArgs() : cont
                        }],
                    };
            },
        });

        monaco.languages.registerCompletionItemProvider('PEG', {
            provideCompletionItems: function (model, position) {
                const suggestions = [
                    {
                        label: "🥚hm...",
                        kind: monaco.languages.CompletionItemKind.Function,
                        documentation: "That's an easter egg 🥚",
                        insertText: 'a bug might become a feature 🥚',
                    }
                ];
                return { suggestions: suggestions };
            }
        });
        // remove completion showing
        window.editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Space, function () { })

        // TODO: remove duplicate code
        window.editor.removeMarkers = function () {
            window.editor.hasMarkers = false
            monaco.editor.setModelMarkers(window.editor.getModel(), "owner", [])
        }

        window.playground.removeMarkers = function () {
            window.playground.hasMarkers = false
            monaco.editor.setModelMarkers(window.playground.getModel(), "owner", [])
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
    })
}

init()
