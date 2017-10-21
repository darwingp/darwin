# push307

Versatile GP system that supports the evolution of both Push programs and Plush genomes. Specifically
designed to solve a symbolic regression problem and a pathfinding problem. This was implemented for
Hamilton College's CPSCI 307 (Genetic Programming) class as taught by professor Thomas Helmuth.
This is the work of Jack Hay and Nate Symer.

## Usage

There are two ways to run the main GP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.

## Idea for Expansion

New GP system: Plush, Behavioral diversity, and Gene-level ALPS (age based genetic hotspots)

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

## TODO

### Nate (up to virtual machine)

- [ ] Adapt GP to use Plush genomes instead of push programs
   - [x] Generation operators
   - [x] Mutation operators
   - [x] Selection operators
   - [x] Crossover operators
   - [ ] Use these changes in pushgp.clj
- [ ] Genetic Hotspots through Age epigenetic marker - like ALPS
- [x] Replace calls to random-choice with rand-nth for clarity
- [ ] Improve terminology and variable names
  - [ ] Things like error vs fitness and :total-error vs overall-error
- [ ] Mock out random numbers for testing purposes
   - https://github.com/trystan/random-seed

### Jack (virtual machine out)

- [ ] Virtual Machine
- [ ] Move hardcoded percentages and GP parameters to core.clj
  - [ ] Hardcoded params in pushgp.clj
  - [ ] Look through other files too
- [ ] Measure behavior diversity
  - [ ] Figure out how to calculate this based on output from the VM
  - [ ] Implement it!
- [ ] UI
  - [ ] Clean up special cases for average (refactor)
  - [ ] Adapt for differing screen sizes
  - [ ] Implement drawing in terms of frames, not modifying UI state.
- [ ] More Push instructions - these need to manipulate a stack of VM instructions

### Other

- [ ] Implement CI pipeline strategy
- [ ] Trace system for maintaining diversity

### Machine specifications

File Specifications:
- ```angle 0```  "angle" change and integer value in degrees
- ```-``` Dash represents no change to heading, vehicle will move one increment by current recorded speed and angle
