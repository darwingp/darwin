# Darwin

Versatile GP system that supports the evolution of both Push programs and Plush genomes with a
lush configuration system and modest complement of genetic programming tools (Plush translation,
testing, crossover operators, selection operators) and an easy framework for defining your own
push instructions and operators..

This genetic programming system was originally designed to solve a symbolic regression problem
and a pathfinding problem for Hamilton College's CPSCI 307 (Genetic Programming) class as taught
by professor Thomas Helmuth. This was originally the work of Jack Hay and Nate Symer, but we welcome
you, programmers of thd internet, to contribute!

Darwin also paralellizes both the initial generation of individuals, the evaluation of individuals,
and applications of genetic operators. Darwin is therefore well-suited to problems that require a
significant amount computation.

## Usage

This is a standard clojure project using lein:

     1. `$ lein run`
     2. Load the project into a REPL (like `lein repl`) and evaluate `(-main)`.

## Problems

### Symbolic Regression (src/darwin/problems/symbolicregression.clj)

  - Solve a basic symbolic regression
  - Example problem

### Pathfinding (src/darwin/problems/pathfinding.clj)

  - Original Ideas: Plush, Behavioral diversity, and Gene-level ALPS (gene-age based genetic hotspots)

Car driver
  - Avoid obstacles
  - Continue after crashes
  - Generate a program that builds up instructions in the movement stack
  - Instructions are fed into a "virtual machine"
  - the evolved program (a driver) is fit to any map

Virtual machine
 - takes a map & starting car position
 - VM instructions (left, right, back, forward, and turn)
 - metrics (how many instrs used, distance, finished or not, # crashes, location of crashes)
   - These metrics could translate into behavioral diversity

#### Notes

 - Pathfinding will likely require several runs to get a satisfactory start and avoid premature convergence or static size.

## Terminology

*testcase* -> A function that takes a final Push state and returns either
	      1. A numerical error value.
	      2. A map with a key :error. All non-:error keys are assimilated
	         into the individual being tested.

*inputs* -> A list of input values to be used to create the :input attribute on an individual.
	    Order matters. The plural is inputses. TODO: better terminology

*gene* -> A hash map containing a key :value which denotes the value of the gene after
          translation and other keys representing epigenetic markers. Some epigenetic markers include:
    {
      :silent false ; if set to true, the gene is not expressed.
      :close 0 ; the number of close parens to insert after the gene.
      :no-op false ; If true, the gene is noop'd. The gene still affects genome translation.
      :arity 0 ; The arity of a push instruction. Affects parenthesization on the exec stack.
      :heat 0 ; How hot a gene is. The notion of heat depends on the implementation of `darwin.gp.hotspots/hot?'.
    }

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

- [ ] Polyploidy
- [ ] More instructions
  - [x] integer-dup
  - [x] integer-frombool
  - [x] boolean-and
  - [x] boolean-or
  - [x] exec-if
  - [ ] exec-dotimes
  - [x] exec-dup
  - [ ] SK(I) combinator calculus instructions for all stacks
- [ ] Stack-agnostic abstract instructions
- [ ] Constant documentation and refactoring
- [ ] Separate out input (in1, in2, ...) frequency

### Symbolic Regression problem

- [ ] Clean up and make into a presentable, well-documented example

### Pathfinding problem

- [ ] Document VM instruction format/structure

### Testing

- [ ] Mock out random numbers for testing purposes
   - https://github.com/trystan/random-seed- [ ] Mock

### Machine specifications

TODO: update me to reflect new format:
  - Instructions are stored using Clojure's read-string (fetched with prn-str)
  - Same format, different representation

File/list Specification:
- ```angle n```  "angle" change and integer value in degrees 0 - ~180 (calculated with standard distribution)
- ```if-obs-range <range> angle <angle> ... angle <angle>``` Triangular brackets refer to integers.  This only makes the moves listed on the same line if there is no intersection within the set range (argument 2) based on a field of view (configured degree value) and the vehicle's current angle (gets three pts based on current angle, current angle +/- fov)
- ```loop <times> angle <angle> ... angle <angle>``` This repeats the list of instructions on the line the number of times that the second parameter provides.
- ```move-while <range> angle <angle> ... angle <angle>``` This loops through the instructions provided on the line.  Before each, it checks for an intersection.  If none, loops back to beginning.
- ```set-speed <speed>``` Sets vehicle speed (per move)
