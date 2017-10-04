# push307

Evolves a program to compute a symbolic regression. This was implemented for Hamilton College's CS 307: Genetic Programming class. This is the work of Jack Hay and Nate Symer.

## Usage

There are two ways to run the main PushGP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.

## TODO

### Nate
- [x] Tournament Selection
- [x] Lexicase Selection
- [x] Epsilon-Lexicase Selection
- [x] Tests and details for push system
### Jack
- [x] Implement alternation crossover
- [ ] Dial in gaussian noise
- [x] Implement crossover operators
- [x] Implement uniform addition, uniform deletion, uniform mutation
- [ ] Update standard noise deviation
- [ ] Refactor uniform operators
- [ ] Work on Tests
- [x] Monitoring and visualizations
- [x] Random generation of push programs
- [x] Crossover, generation
- [x] Mutation
- [ ] Random number scheme for testing
- [ ] Work on individual format
### Other
- [ ] Implement CI pipeline strategy
- [ ] Trace system for maintaining diversity
