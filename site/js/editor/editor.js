import * as themes from "./themes.js";
import { config, hoverHints, tokensProvider, editorOptions, defaultGrammar } from "./editorConstants.js";
import { updateFontSize, updateAstView } from "../loader.js"
import { addPlaceholdersWithOnInput } from "./placeholder.js";
import { addHotkeys } from "./hotkeys.js";

window.myGrammar = new window.Interpreter.result.Pipeline()

function init() {
    require(['vs/editor/editor.main'], function () {
        monaco.languages.register({ id: "PEG" });
        monaco.languages.setLanguageConfiguration("PEG", config);
        monaco.languages.setMonarchTokensProvider("PEG", tokensProvider);

        monaco.editor.defineTheme("PEG-light", themes.light);
        monaco.editor.defineTheme("PEG-dark", themes.dracula);

        let storedGrammar = localStorage.getItem("playground")

        window.editor = monaco.editor.create(document.getElementById("editor"), {
            ...editorOptions,
            value: storedGrammar != null ? storedGrammar : defaultGrammar,
            language: "PEG"
        });

        window.textEditor = monaco.editor.create(document.getElementById("text-editor"), {
            ...editorOptions
        });

        window.ast = monaco.editor.create(document.getElementById("ast"), {
            ...editorOptions,
            folding: true
        });

        var didScrollChangeDisposable = window.ast.onDidScrollChange(function () {
            didScrollChangeDisposable.dispose();
            if (localStorage.getItem("ast") == "false") {
                setTimeout(() => {
                    updateAstView(false)
                }, 1000)
            }
        });

        addPlaceholdersWithOnInput()

        updateFontSize()

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
            provideCompletionItems: function (_model, _position) {
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

        addHotkeys()
    })
}

init()
