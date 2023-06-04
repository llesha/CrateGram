import { getTheme } from "../loader.js";

const config = {
    surroundingPairs: [
        {
            open: "[",
            close: "]",
        },
        {
            open: "(",
            close: ")",
        },
        {
            open: '"',
            close: '"',
        },
        {
            open: "'",
            close: "'",
        },
    ],
    autoClosingPairs: [
        {
            open: "[",
            close: "]",
        },
        {
            open: "(",
            close: ")",
        },
        {
            open: '"',
            close: '"',
            notIn: ["apos", "comment"],
        },
        {
            open: "'",
            close: "'",
            notIn: ["string", "comment"],
        },
    ],
    // brackets: [
    //     ["[", "]"],
    //     ["(", ")"],
    // ],
};

const tokenizer = {
    root: [
        // identifiers and keywords
        [/\./, "identifier"],
        [
            /[a-z_][\w]*/,
            {
                cases: {
                    "@keywords": "keyword",
                    "@default": "identifier",
                },
            },
        ],
        [/[A-Z][\w]*/, "identifier"],

        // whitespace
        {
            include: "@whitespace",
        },
        [/#.*/, "comment"],

        // delimiters and operators
        [/[{}()]/, "@brackets"],
        [
            /@symbols/,
            {
                cases: {
                    "=": "operator.equal",
                    "<-": "operator.equal",
                    "@operators": "operator",
                    "@default": "invalid",
                },
            },
        ],

        // [/\d+/, "number"],

        // strings
        [/"([^"\\]|\\.)*$/, "string"], // non-terminated string
        [
            /"/,
            {
                token: "string",
                bracket: "@open",
                next: "@string",
            },
        ],
        [/'([^'\\]|\\.)*$/, "string"], // non-terminated string
        [
            /'/,
            {
                token: "string",
                bracket: "@open",
                next: "@apos",
            },
        ],

        [/\[([^\]\\]|\\.)*/, "bracket"], // non-terminated string
        [
            /\[/,
            {
                token: "bracket",
                bracket: "@open",
                next: "@characterClass",
            },
        ],
        [/\]/, "bracket"],
        [/[^\]]/, "invalid"] // everything else
    ],

    comment: [
        [/[^\/*]+/, "comment"],
        [/\(\*/, "comment", "@push"], // nested comment
        [/\*\)/, "comment", "@pop"],
        [/[\/*]/, "comment"],
    ],

    string: [
        [/[^\\"]+/, "string"],
        [/@escapes/, "string.escape"],
        [/\\./, "string.escape.invalid"],
        [
            /"/,
            {
                token: "string",
                bracket: "@close",
                next: "@pop",
            },
        ],
    ],

    apos: [
        [/[^\\']+/, "apos"],
        [/@escapes/, "string.escape"],
        [/\\./, "string.escape.invalid"],
        [
            /'/,
            {
                token: "string",
                bracket: "@close",
                next: "@pop",
            },
        ],
    ],

    characterClass: [
        // [/[^\\\[]+/, "characterClass"],
        // [/@escapes/, "string.escape"],
        // [/\\./, "string.escape.invalid"],
        [
            /\]/,
            {
                token: "bracket",
                bracket: "@close",
                next: "@pop",
            },
        ],
    ],

    whitespace: [
        [/[ \t\r\n]+/, "white"],
        [/\(\*/, "comment", "@comment"]
    ]
}

const groupHint = `<strong>Group</strong> - groups tokens into one`
const classHint = `<strong>Character Class</strong> - matches<br>a single character from set `
const orHint = `<strong>Choice</strong> - equivalent to boolean OR`
const ruleHint = `<strong>Rule</strong> - declares a rule with name<br> similar to previous token`

const hoverHints = {
    "*": `<strong>Quantifier</strong> - matches the previous token<br>
    between <strong>zero</strong> and unlimited times,<br>
    as many times as possible`,
    "+": `<strong>Quantifier</strong> - matches the previous token<br>
    between <strong>one</strong> and unlimited times,<br>
    as many times as possible`,
    "?": `<strong>Optional</strong> - matches the previous token<br>
    between <strong>zero</strong> and <strong>one</strong> time,<br> 
    as many times as possible`,
    ".": `matches any character (except line break)`,

    "[": classHint,
    "]": classHint,

    "&": `<strong>And-predicate</strong> - requires following token<br>to be present,
    does not consume it`,
    "!": `<strong>Not-predicate</strong> - requires following token<br>to be absent,
    does not consume it`,

    "/": orHint,
    "|": orHint,
    "(": groupHint,
    ")": groupHint,

    "=": ruleHint,
    "<-": ruleHint,
    "-": `<strong>Range</strong> - matches a single character<br> in the
    range between <br>`
}

const tokensProvider = {
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
}


const editorOptions = {
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

const defaultGrammar = `# Hello world
root =   welcome COMMA SPACE* subject punctuation !.

welcome = ("Hello" | "Greetings" | "Salute") SPACE*
subject = [A-Z][a-z]* SPACE*
punctuation = [!?.] SPACE*


COMMA = "," 
SPACE = " "`

export { config, hoverHints, tokensProvider, editorOptions, defaultGrammar }