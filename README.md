# push307

Evolves a program to compute a symbolic regression. This was implemented for Hamilton College's CS 307: Genetic Programming class. This is the work of Jack Hay and Nate Symer.

## Usage

There are two ways to run the main PushGP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.

## TODO

### Nate
- [ ] Crossover operator from class
- [ ] Epsilon-Lexicase Selection
- [x] Tests and details for push system
### Jack
- [x] Implement alternation crossover
- [ ] Implement crossover operators
- [x] Implement uniform addition, uniform deletion, uniform mutation
- [ ] Update standard noise deviation
- [ ] Refactor uniform operators
- [ ] Work on test
### Other
- [ ] Split push307.core into more logical namespaces
- [ ] Split tests into more logical namespaces
- [ ] Figure out how to get generative testing working
- [ ] Write unit, integration, and validity tests for push interpreter
- [ ] Implement CI pipeline strategy
- [ ] Implement push operations
- [ ] Implement interpret-one-step
- [ ] Work on GP system
- [ ] Trace system for maintaining diversity
