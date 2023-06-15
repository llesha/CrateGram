![img](/site/resources/crategram-github.svg)

A parse utility for [PEG parsers](https://pdos.csail.mit.edu/~baford/packrat/popl04/peg-popl04.pdf), which includes:
1. Grammar tasks
2. PEG Playground
3. Abstract syntax tree of input text

## [Extended] PEG Grammar

### Definitions

**Terminal** is a literal symbol.

**Non-terminal** is a rule name, that gets replaced by a **parsing expression** (denoted as e in the following table)
defined after `=` or `<-`[^1] symbol in the rule.

**Parsing expression** defines how input should be consumed to form an Abstract Syntax Tree.

| Operator                 | Precedence | Description                                                                                            |
|--------------------------|------------|--------------------------------------------------------------------------------------------------------|
| `' '` or `" "`           | 5          | Literal string                                                                                         |
| `[ ]`                    | 5          | Character class - matches a single character from set. Can use range[^2]                               | 
| `.`                      | 5          | Any character - matches any character except line break                                                | 
| `(e)`                    | 5          | Grouping - groups tokens into one                                                                      | 
| `e?`                     | 4          | Optional - matches the previous token between zero and one time, as many times as possible             | 
| `e*`                     | 4          | Zero-or-more - matches the previous token  between zero and unlimited times, as many times as possible | 
| `e+`                     | 4          | One-or-more - matches the previous token  between one and unlimited times, as many times as possible   | 
| `e{n}`                   | 4          | **[Extended]** Exactly n - matches previous token exactly `n` times                                    | 
| `&e`                     | 3          | And-predicate - requires following token to be present, does not consume it                            | 
| `!e`                     | 3          | Not-predicate - requires following token to be absent, does not consume it                             | 
| `e1 e2`                  | 2          | Sequence - matches a sequence of terminals or non-terminals                                            |
| `e1 / e2`  or `e1 \| e2` | 1          | Prioritized Choice - equivalent to boolean OR                                                          |
| `# ...`                  | -          | **[Extended]** Comment - line comment                                                                  | 
| `(* ... *)`              | -          | Multiline comment                                                                                      | 

## Grammar tasks 
Grammar tasks are inspired by the [Caterpillar logic]() game. 

In each task, a user has to construct a grammar that would be similar to a task grammar (which is hidden) by testing different inputs. For each input the program says whether it is valid for the task grammar or not. To make it easier, each task already has 5 valid and invalid inputs. 

### Example
Suppose we are given a task and we know that following inputs are valid:
```
01010101
101
11100001
110101011
00110
```

And following are invalid:
```
0
1
1111
00
0000
```

We can assume that input is valid if it contains both 1 and 0.
Let's create a grammar and test it:

```
root = zeroStart / oneStart
zeroStart = "0"+ "1"+ any
oneStart = "1"+ "0"+ any
any = [01]*
```

We get: WA: 101
It is wrong because we need even number of ones. Therefore, this grammar should work:

```
root = zeroFirst / zeroMiddle / zeroLast
ones = ("1" "0"* "1")
zeroFirst = "0"+ ones ones* "0"*
zeroLast = "0"* ones ones* "0"+
zeroMiddle = "11"* ("1" "0"+ "1")+ "11"*
```

[^1]: `=` and `<-` in the rule definition are interchangeable

[^2]: Range: `X-Y` matches a single character in the
range between `X` and `Y`. E.g. `a-z` matches any non-capital english letter
