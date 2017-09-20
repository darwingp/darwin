(ns push307.core
  (:require [push307.push :refer :all])
  (:require [push307.push.utilities :refer :all])
  (:require [push307.push.instructions :refer :all])
  (:gen-class))


; An example Push state
(def example-push-state
  {:exec '(integer_+ integer_-)
   :integer '(1 2 3 4 5 6 7)
   :string '("abc" "def")
   :input {:in1 4 :in2 6}})

; An example Push program
(def example-push-program
  '(3 5 integer_* "hello" 4 "world" integer_-))

; An example individual in the population
; Made of a map containing, at mimimum, a program, the errors for
; the program, and a total error
(def example-individual
  {:program '(3 5 integer_* "hello" 4 "world" integer_-)
   :errors [8 7 6 5 4 3 2 1 0 1]
   :total-error 37})


;;;;;;;;;;
;; Instructions must all be either functions that take one Push
;; state and return another or constant literals.
(def instructions
  (list
   'in1
   'integer_+
   'integer_-
   'integer_*
   'integer_%
   0
   1
   ))


;;;;;;;;;;
;; Interpreter

(defn interpret-one-step
  "Helper function for interpret-push-program.
  Takes a Push state and executes the next instruction on the exec stack,
  or if the next element is a literal, pushes it onto the correct stack.
  Returns the new Push state."
  [push-state]
  :STUB
  )

(defn interpret-push-program
  "Runs the given program starting with the stacks in start-state. Continues
  until the exec stack is empty. Returns the state of the stacks after the
  program finishes executing."
  [program start-state]
  ;; NB: This code will reverse program twice. Is that bad?
  (loop [st (push-many-to-stack
              start-state
              :exec
              (reverse program))] ;; push the program to the exec stack
    (if (empty-stack? st :exec)
      st
      (recur (interpret-one-step st)))))


;;;;;;;;;;
;; GP

(defn make-random-push-program
  "Creates and returns a new program. Takes a list of instructions and
  a maximum initial program size."
  [instructions max-initial-program-size]
  :STUB
  )

(defn tournament-selection
  "Selects an individual from the population using a tournament. Returned
  individual will be a parent in the next generation. Can use a fixed
  tournament size."
  [population]
  :STUB
  )

(defn crossover
  "Crosses over two programs (note: not individuals) using uniform crossover.
  Returns child program."
  [prog-a prog-b]
  :STUB
  )

(defn uniform-addition
  "Randomly adds new instructions before every instruction (and at the end of
  the program) with some probability. Returns child program."
  [prog]
  :STUB
  )

(defn uniform-deletion
  "Randomly deletes instructions from program at some rate. Returns child program."
  [prog]
  :STUB
  )

(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. Gives 50% chance to crossover,
  25% to uniform-addition, and 25% to uniform-deletion."
  [population]
  :STUB
  )

(defn report
  "Reports information on the population each generation. Should look something
  like the following (should contain all of this info; format however you think
  looks best; feel free to include other info).

-------------------------------------------------------
               Report for Generation 3
-------------------------------------------------------
Best program: (in1 integer_% integer_* integer_- 0 1 in1 1 integer_* 0 integer_* 1 in1 integer_* integer_- in1 integer_% integer_% 0 integer_+ in1 integer_* integer_- in1 in1 integer_* integer_+ integer_* in1 integer_- integer_* 1 integer_%)
Best program size: 33
Best total error: 727
Best errors: (117 96 77 60 45 32 21 12 5 0 3 4 3 0 5 12 21 32 45 60 77)
  "
  [population generation]
  :STUB
  )

(defn push-gp
  "Main GP loop. Initializes the population, and then repeatedly
  generates and evaluates new populations. Stops if it finds an
  individual with 0 error (and should return :SUCCESS, or if it
  exceeds the maximum generations (and should return nil). Should print
  report each generation.
  --
  The only argument should be a map containing the core parameters to
  push-gp. The format given below will decompose this map into individual
  arguments. These arguments should include:
   - population-size
   - max-generations
   - error-function
   - instructions (a list of instructions)
   - max-initial-program-size (max size of randomly generated programs)"
  [{:keys [population-size max-generations error-function instructions max-initial-program-size]}]
  :STUB
  )


;;;;;;;;;;
;; The functions below are specific to a particular problem.
;; A different problem would require replacing these functions.
;; Problem: f(x) = x^3 + x + 3

(defn target-function
  "Target function: f(x) = x^3 + x + 3
  Should literally compute this mathematical function."
  [x]
  :STUB
  )

(defn regression-error-function
  "Takes an individual and evaluates it on some test cases. For each test case,
  runs program with the input set to :in1 in the :input map part of the Push state.
  Then, the output is the integer on top of the integer stack in the Push state
  returned by the interpreter. Computes each error by comparing output of
  the program to the correct output.
  Returns the individual with :errors set to the list of errors on each case,
  and :total-error set to the sum of the errors.
  Note: You must consider what to do if the program doesn't leave anything
  on the integer stack."
  [individual]
  :STUB
  )


;;;;;;;;;;
;; The main function. Uses some problem-specific functions.

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (push-gp {:instructions instructions
            :error-function regression-error-function
            :max-generations 500
            :population-size 200
            :max-initial-program-size 50}))
