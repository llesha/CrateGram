# Branch description

**two-child-group** - branch with only two children in `Group` and `Or`. It was made for grammar reduction described in the [original paper](https://pdos.csail.mit.edu/~baford/packrat/popl04/peg-popl04.pdf). The problem with this design is building AST for `Repeated`, where a list of nested `Group`s should be flattened.