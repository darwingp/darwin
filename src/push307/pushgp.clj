(ns push307.pushgp
  (:gen-class))

  ;;;;;;;;;;
  ;; GP
  ; ; An example individual in the population
  ; ; Made of a map containing, at mimimum, a program, the errors for
  ; ; the program, and a total error
  ; (def example-individual
  ;   {:program '(3 5 integer_* "hello" 4 "world" integer_-)
  ;    :errors [8 7 6 5 4 3 2 1 0 1]
  ;    :total-error 37})

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
    ;epsilon
    [population]
    :STUB
    )

  (defn crossover
      "Crosses over two programs (note: not individuals) using uniform crossover.
      Returns child program."
      ;alternation
      [prog-a prog-b]
      :STUB
      )

  (defn add-noise
    "returns gaussian noise for alternation crossover index modification"
    ; CITE: https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
    ; DESC: The Box-Muller method for generating uniformly distributed random numbers 
    [alignment]   ;standard deviation (usually around 10)
    
  )

  (defn alternation-crossover
    "Crosses over two programs (note: not individuals) using alternation crossover
    takes alternation rate and alignment-deviation"
    ;alternation
    [prog-a prog-b, alternation-rate, alignment-deviation]
    (loop [index 0 child '() pa prog-a pb prog-b] 
      (if (= index (count prog-a))
        ;if a list is of different lengths, nil elements will be added and must be removed
        (filter #(not= nil %) (reverse child))    
        (do
          (if (< (rand) alternation-rate)
            ;TODO: add gaussian noise to index (to true recur index)
            (recur (+ index 1) (cons (first pa) child) (rest pb) (rest pa))
             (recur (+ index 1) (cons (first pa) child) (rest pa) (rest pb))
      )))))
;; These parens are to make it compile so that tests could be ran

;; They are deletable

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
