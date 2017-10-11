# push307

Evolves a program to build another program that moves a car through a given maze. This was 
implemented for Hamilton College's CS 307: Genetic Programming class. This is the work of Jack Hay and Nate Symer.

## Usage

There are two ways to run the main PushGP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.

## Idea for Expansion

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

- [ ] Push interpreter nested
- [ ] Plush Genome

### Jack (virtual machine out)

- [ ] Measure behavior diversity - VM
- [ ] UI
- [ ] UI - Tendlines for fitnesses
- [ ] Start working on VM

### Ongoing

- [ ] Virtual machine
- [ ] Make decisions on things like error/fitness
- [ ] More Push instructions - these need to manipulate a stack of VM instructions

### Other

- [ ] Implement CI pipeline strategy
- [ ] Trace system for maintaining diversity
