# Darwin

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
  - the evolved program (a driver) is fit to a specific map

Virtual machine
 - takes a map & starting car position
 - VM instructions (left, right, back, forward, and turn)
 - metrics (how many instrs used, distance, finished or not, # crashes, location of crashes)
   - These metrics could translate into behavioral diversity

## Terminology

*testcase* -> A function that takes a final Push state and returns a numerical error value.

*inputs* -> A list of input values to be used to create the :input attribute on an individual.
	    Order matters.

*gene* -> A hash map containing a key :value which denotes the value of the gene after
          translation and other keys representing epigenetic markers. Some epigenetic markers include:
  - `:silent` -> if set to true, the gene is not expressed.
  - `:close` -> the number of close parens to insert after the gene
  - `:no-op` -> No-ops the gene. The gene still affects genome translation.
  - `:arity` -> The arity of a push instruction. Affects parenthesization.

*individual* -> A map containing the following keys:

    {
      :program '()   ; A list of instructions and literals
      :genome '()    ; A list of @gene@s
      :total-error 0 ; A numeric value equal to the sum of all errors
      :errors '()    ; A list of numeric values, where a value at index n corresponds to
                     ; the error on the nth test case.
      :exit-states   ; the Push states reached by running the individual's :program on a series of inputses
    }

		All of these keys are available to crossover/selection operators and are guaranteed before
                select-and-vary in darwin.gp is called.

*genome* -> A list of genes

*population* -> A list of individuals
*generation* -> A population at a given iteration of evolution.

## TODO

### Both

- [ ] Polyploidy

### Nate (up to virtual machine)

- [ ] Genetic Hotspots through :age epigenetic marker - like ALPS
    - [ ] This genetic marker is untouched by translation; instead it's
          used solely by genetic operators.
- [x] Adapt GP to use Plush genomes in addition to push programs
   - [x] Generation operators
   - [x] Mutation operators
   - [x] Selection operators
   - [x] Crossover operators
   - [x] Rewrite translation
   - [x] Run testcases on genomic individuals
   - [x] Make genetic operators genome-aware by making them take individuals rather than programs
- [x] Implement :behavioral-diversity GP param (func that calcs behavioral diversity)
- [x] Transforms on individuals
- [x] Implement percentages for crossover and selection operators
- [x] Figure out where to run the machine - in tests or in transform
- [x] Generalize select-and-vary
  - [x] run-gp parameter for selection operator
        (a function that takes a population and returns an individual)
  - [x] run-gp parameter for crossover operator
        (a function that takes two individuals and returns a new individual)
  - [x] generalize the percentages of all operators too
- [x] Figure out how to calculate error of the generated instructions
  - How can one run provide multiple error values?
    - currently one run per error value
- [x] Replace calls to random-choice with rand-nth for clarity
- [x] Improve terminology and variable names
  - [x] Things like error vs fitness and :total-error vs overall-error
  - [x] Document format for individuals, genes, etc.

### Jack (virtual machine out)

- [x] Virtual Machine
  - [x] Virtual machine instructions
  - [x] Replace noop with zero rotate
  - [x] Load obstacles from file
- [ ] Allow nested conditional/loops
- [x] Measure behavior diversity
  - [x] Figure out how to calculate this based on output from the VM
  - [x] Implement it!
  - [x] Angle noise distribution
- [x] Push instructions - these need to manipulate a stack of VM instructions
- [x] Novelty, archive
- [x] Define module under darwin.problems.pathfinding for the driver problem.
  - [x] Move driver VM to submodules of darwin.problems.pathfinding

NOTE*** After all TODO items are complete, we need to check each other's work.

### Instructions to Implement

- integer-dup
- integer-frombool

- boolean-and
- boolean-or

- exec-if
- exec-dotimes
- exec-dup

- SK(I) combinator calculus instructions for all stacks?

"move" stack
- move-rotate -> pushes

### Other

- [ ] Implement CI pipeline strategy
- [ ] Trace system for maintaining diversity
- [ ] Mock out random numbers for testing purposes
   - https://github.com/trystan/random-seed

### Machine specifications

File/list Specification:
- ```angle n```  "angle" change and integer value in degrees 0 - ~180 (calculated with standard distribution)
- ```if-obs-range <range> angle <angle> ... angle <angle>``` Triangular brackets refer to integers.  This only makes the moves listed on the same line if there is no intersection within the set range (argument 2) based on a field of view (configured degree value) and the vehicle's current angle (gets three pts based on current angle, current angle +/- fov)
- ```loop <times> angle <angle> ... angle <angle>``` This repeats the list of instructions on the line the number of times that the second parameter provides.
- ```move-while <range> angle <angle> ... angle <angle>``` This loops through the instructions provided on the line.  Before each, it checks for an intersection.  If none, loops back to beginning.
- ```set-speed <speed>``` Sets vehicle speed (per move)
