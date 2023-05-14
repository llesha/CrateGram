# ParseBox

A parse utility for [PEG parsers](https://pdos.csail.mit.edu/~baford/packrat/popl04/peg-popl04.pdf)

## [Extended] PEG Grammar

### Definitions
**Terminal** is a literal symbol.

**Non-terminal** is a rule name, that gets replaced by a **parsing expression** (denoted as e in the following table) defined after `=` or `<-`[^1] symbol in the rule.

**Parsing expression** defines how input should be consumed to form an Abstract Syntax Tree.


| Operator                 | Type         | Precedence | Description                                                                                            |
|--------------------------|--------------|------------|--------------------------------------------------------------------------------------------------------|
| `' '` or `" "`           | primary      | 5          | Literal string                                                                                         |
| `[ ]`                    | primary      | 5          | Character class - matches a single character from set. Can use range[^2]                               | 
| `.`                      | primary      | 5          | Any character - matches any character expect line break                                                | 
| `(e)`                    | primary      | 5          | Grouping - groups tokens into one                                                                      | 
| `e?`                     | unary suffix | 4          | Optional - matches the previous token between zero and one time, as many times as possible             | 
| `e*`                     | unary suffix | 4          | Zero-or-more - matches the previous token  between zero and unlimited times, as many times as possible | 
| `e+`                     | unary suffix | 4          | One-or-more - matches the previous token  between one and unlimited times, as many times as possible   | 
| `e{n}`                   | unary suffix | 4          | **[Extended]** Exactly n - matches previous token exactly `n` times                                    | 
| `&e`                     | unary prefix | 3          | And-predicate - requires following token to be present, does not consume it                            | 
| `!e`                     | unary prefix | 3          | Not-predicate - requires following token to be absent, does not consume it                             | 
| `e1 e2`                  | binary       | 2          | Sequence - matches a sequence of terminals or non-terminals                                            |
| `e1 / e2`  or `e1 \| e2` | binary       | 1          | Prioritized Choice - equivalent to boolean OR                                                          | 


[^1]: `=` and `<-` in the rule definition are interchangeable

[^2]: Range: `X-Y` matches a single character in the
range between `X` and `Y`. E.g. `a-z` matches any non-capital english letter
