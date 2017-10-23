# darwin

Versatile GP system that supports the evolution of both Push programs and Plush genomes. Specifically
designed to solve a symbolic regression problem and a pathfinding problem. This was implemented for
Hamilton College's CPSCI 307 (Genetic Programming) class as taught by professor Thomas Helmuth.
This is the work of Jack Hay and Nate Symer.

## Usage

There are two ways to run the main GP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.

## Idea for Expansion

New GP system: Plush, Behavioral diversity, and Gene-level ALPS (gene-age based genetic hotspots)

Car driver
  - Avoid obstacles
  - Continue after crashes
  - Generate a program that builds up instructions in the movement stack
  - Instructions are fed into a "virtual machine"
  - the evolved program (an autodriver) is fit to a specific map

Virtual machine
 - takes a map & starting car position
 - VM instructions (left, right, back, forward, and turn)
 - metrics (how many instrs used, distance, finished or not, # crashes, location of crashes)
   - These metrics could translate into behavioral diversity

## Terminology

"testcase" -> A function that takes a Push program and returns a numberical error value.
              These are usually created/declared through the testcase macro.

"gene" -> A hash map containing a key :instruction which denotes the value of the gene
          and other keys representing epigenetic markers. Some epigenetic markers include:
  - `:silent` -> if set to true, the gene is not expressed.
  - `:close` -> the number of close parens to insert after the gene
  - `:no-op` -> No-ops the gene. The gene still affects genome translation.
  - `:arity` -> The arity of a push instruction. Affects parenthesization.

"individual" -> A hashmap containing the following keys:
  {
    :program '()   ; A list of instructions and literals
    :genome '()    ; A list of @gene@s
    :total-error 0 ; A numeric value equal to the sum of all errors
    :errors '()    ; A list of numeric values, where a value at index n corresponds to
                   ; the error on the nth test case.
  }
"genome" -> A list of genes

"population" -> A list of individuals
"generation" -> A list; At any given state of the GP algorithm, the set of individuals
                who are ontologically related to the initial population and
                have similar lineage, as well as any programs introduced into that set.

## TODO

### Nate (up to virtual machine)

- [x] Adapt GP to use Plush genomes in addition to push programs
   - [x] Generation operators
   - [x] Mutation operators
   - [x] Selection operators
   - [x] Crossover operators
   - [x] Rewrite translation
   - [x] Run testcases on genomic individuals
   - [x] Make genetic operators genome-aware by making them take individuals rather than programs
- [ ] Generalize select-and-vary
  - [ ] run-gp parameter for selection
        (a function that takes a population and returns an individual)
  - [ ] run-gp parameter for crossover
        (a function that takes two individuals and returns a new individual) 
- [ ] Genetic Hotspots through :age epigenetic marker - like ALPS
    - [ ] This genetic marker is untouched by translation; instead it's
          used solely by genetic operators.
- [x] Replace calls to random-choice with rand-nth for clarity
- [x] Improve terminology and variable names
  - [x] Things like error vs fitness and :total-error vs overall-error
  - [x] Document format for individials, genes, etc.

### Jack (virtual machine out)

- [ ] Virtual Machine
  - [ ] Virtual machine instructions
- [ ] Move hardcoded percentages and GP parameters to core.clj
  - [ ] Hardcoded params in gp.clj
  - [ ] Look through other files too
- [ ] Measure behavior diversity
  - [ ] Figure out how to calculate this based on output from the VM
  - [ ] Implement it!
- [ ] UI
  - [ ] Clean up special cases for average (refactor)
  - [ ] Adapt for differing screen sizes
  - [ ] Implement drawing in terms of frames, not modifying UI state.
- [ ] Push instructions - these need to manipulate a stack of VM instructions
- [ ] Push exec_ instructions

NOTE*** After all TODO items are complete, we need to check each other's work.

### Other

- [ ] Implement CI pipeline strategy
- [ ] Trace system for maintaining diversity
- [ ] Mock out random numbers for testing purposes
   - https://github.com/trystan/random-seed

### Machine specifications

File Specifications:
- ```angle 0```  "angle" change and integer value in degrees
- ```-``` Dash represents no change to heading, vehicle will move one increment by current recorded speed and angle
