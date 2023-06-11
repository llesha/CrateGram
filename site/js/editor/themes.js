var dracula = {
    base: "vs-dark",
    inherit: true,
    rules: [{
        token: 'identifier',
        foreground: "cfcfc2"
    },
    {
        token: 'terminal',
        foreground: '000000'
    },
    {
        token: 'number',
        foreground: "e69749"
    },
    {
        foreground: "6272a4",
        token: "comment",
        fontStyle: 'italic'
    },
    {
        token: "string",
        foreground: "f1fa8c",
    },
    {
        token: 'string.escape',
        foreground: 'a2a2a2'
    },
    {
        token: "apos",
        foreground: "f1fa8c",
    },
    {
        token: "keyword",
        foreground: "cfcfc2",
        fontStyle: "bold"
    },
    {
        token: "invalid",
        foreground: "ff0303",
        background: "ff79c6",
    },
    {
        token: 'string.escape.invalid',
        foreground: 'ff0303'
    },
    {
        token: "operator",
        foreground: "3896d4",
    },
    {
        token: 'operator.equal',
        foreground: '3896d4',
    },
    {
        token: "bracket",
        foreground: "b17215"
    },
        // {
        //     token: "invalid",
        //     foreground: "ff0303"// "D2360F"//"e41f0c",
        // }
    ],
    colors: {
        "editor.foreground": "#cfcfc2",
        "editor.background": "#282a36",
        "editor.selectionBackground": "#44475a",
        // "editor.lineHighlightBackground": "#44475a",
        "editorCursor.foreground": "#cfcfc2",
        "editorWhitespace.foreground": "#3B3A32",
        "editorIndentGuide.activeBackground": "#9D550FB0",
        "editor.selectionHighlightBorder": "#222218"
    }
}

var light = {
    base: 'vs',
    inherit: false,
    rules: [{
        token: 'identifier',
        //foreground: '820d7c'
    }, {
        token: 'terminal',
        foreground: '000000'
    }, {
        token: 'keyword',
        fontStyle: 'bold'
        //foreground: '6197d3'
    }, {
        token: 'string',
        foreground: '067d17'
    },
    {
        token: 'string.escape',
        foreground: '525252'
    },
    {
        token: 'apos',
        foreground: '067d17'
    },
    {
        token: 'number',
        foreground: '0033b3'
    }, {
        token: 'comment',
        foreground: '6a8759',
        fontStyle: 'italic'
    },
    {
        token: 'operator',
        foreground: '0033b3',
    },
    {
        token: 'operator.equal',
        foreground: '0033b3',
    },
    {
        token: "bracket",
        foreground: "c8511e"
    },
    {
        token: 'string.escape.invalid',
        foreground: 'ff0303'
    },
    {
        token: "invalid",
        foreground: "ff0303"// "D2360F"//"e41f0c",
    }],
    colors: {
        // 'editor.foreground': '#d4d4d4',
        // 'editor.background': '#2b2b2b'
    }
}

export default { dracula };
export { light, dracula };