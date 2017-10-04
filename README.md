# push307

Evolves a program to compute a symbolic regression. This was implemented for Hamilton College's CS 307: Genetic Programming class. This is the work of Jack Hay and Nate Symer.

## Usage

There are two ways to run the main PushGP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.

## Idea for Expansion

1. Plush Genomes
2. Car driver
   a. Avoid obstacles

## TODO

- [ ] Push interpreter nested
- [ ] More instructions
- [ ] Measure Structural Diversity
- [ ] Plush Genome

### Nate
- [ ] Test operators
- [ ] Squash bugs
- [ ] Implement structural diversity
- [x] Tournament Selection
- [x] Lexicase Selection
- [x] Epsilon-Lexicase Selection
- [x] Tests and details for push system
### Jack
- [ ] Autoscale UI plotter
- [ ] Test operators
- [x] Implement alternation crossover
- [ ] Dial in gaussian noise
- [x] Implement crossover operators
- [x] Implement uniform addition, uniform deletion, uniform mutation
- [x] Monitoring and visualizations
- [x] Random generation of push programs
- [x] Crossover, generation
- [x] Mutation

### Pipedream

- [ ] Measure Behavioral Diversity
- [ ] Random number mocking system for tests

### Other
- [ ] Implement CI pipeline strategy
- [ ] Trace system for maintaining diversity
