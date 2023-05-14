import * as themes from "./themes.js";
import { config, hoverHints, tokenizer } from "./editorConstants.js";

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
            symbols: /[=>\.<!?:&|+\-*\/%]+/,
            // C# style strings
            escapes:
                /\\(?:[abfnrtv\\"']|x[\dA-Fa-f]{1,4}|u[\dA-Fa-f]{4}|U[\dA-Fa-f]{8})/,
            tokenizer: tokenizer
        });

        monaco.editor.defineTheme("PEG-light", themes.light);
        monaco.editor.defineTheme("PEG-dark", themes.dracula);

        window.editor = monaco.editor.create(document.getElementById("editor"), {
            value: `Value   = [0-9.]+ / '(' Expr ')'
Product = Expr (('*' / '/') Expr)*
Sum     = Expr (('+' / '-') Expr)*
Expr    = "a" Product / Sum / Value`,
            language: "PEG",
            glyphMargin: true,
            fontFamily: "Fira Code",
            fontLigatures: true,
            theme: `PEG-${localStorage.getItem("theme") == null ? "light" : localStorage.getItem("theme")}`,
            fontSize: 16,
            // automaticLayout: true,
            minimap: {
                enabled: false,
            },
            folding: false,
            lineNumbers: "off",
            lineDecorationsWidth: 0,
            lineNumbersMinChars: 0,
            bracketColorizationOptions: { enabled: false }
        });

        window.editor.addCommand(monaco.KeyMod.Alt | monaco.KeyMod.Shift | monaco.KeyCode.KeyF, function () {
            let tokens = monaco.editor.tokenize(window.editor.getValue(), "PEG")
            //console.log(tokens)
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
                //  let tokens = model.getLineTokens(position.lineNumber);
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
                if (cont == null && value.match(/[^\w\[\]()?*+]/)) {
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
    })
}

init()