(ns push307.pushgp
  (:require [push307.push.utilities :refer :all])
  (:require [push307.pushgp.utilities :refer :all])
  (:require [push307.pushgp.crossover :refer :all])
  (:require [push307.pushgp.selection :refer :all])
  (:require [push307.pushgp.mutation :refer :all])
  (:require [push307.pushgp.generation :refer :all])
 ; (:require [push307.plotter :refer :all])
  (:gen-class))

; ; An example individual in the population
; ; Made of a map containing, at mimimum, a program, the errors for
; ; the program, and a total error
; (def example-individual
;   {:program '(3 5 integer_* "hello" 4 "world" integer_-)
;    :errors [8 7 6 5 4 3 2 1 0 1]
;    :total-error 37})

;Individual attributes:
; :program
; :errors
; :total-error
; :output-behavior (string of actions?)

(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. Gives 50% chance to crossover,
  25% to uniform-addition, and 25% to uniform-deletion."
  [population]
  (let [v (rand-int 100)]
    (new-individual
      (cond
        (< v 50) (uniform-crossover
                   (:program (tournament-selection population 20))
                   (:program (tournament-selection population 20)))
        (< v 75) (uniform-addition
                   (:program (tournament-selection population 20)))
        :else    (uniform-deletion
                   (:program (tournament-selection population 20)))))))

(def indiv-error 
  (fn [x] (:total-error x)))

(defn best-fit
  "takes population and determines best function fitness"
  [population]
  (reduce min (map indiv-error pop))
)

;; Should behavior-diversity, average-error, and lowest-size really return
;; a value from 0 to 100?

(defn behavior-diversity
  "Returns a measure of the behavioral diversity of a population of individuals."
  [population]
  ;returns value 0-100
  20
)

(defn average-error
  "Returns the average error of population of individuals."
  [population]
  ;returns value 0-100
  (/ (reduce +' (map overall-error population)) (count population)))

(defn lowest-size
  "Returns the length of the shortest program in a population of indivudals"
  [population]
  ;returns value 0-100
  (apply min
    (map count
      (map :program population))))

(defn fill-state
  "takes population and creates list of values"
  [pop]
  { :points-fit (best-fit pop)
    :points-behavior (behavior-diversity pop)
    :average-error (average-error pop)
    :lowest-size (lowest-size pop)
    :generation (:gen pop) })

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
  ; TODO: attempt to implement graphical system for real-time graphing
  ; note: need some way of recording previous values
 ;  (let [current-state (fill-state population)]
    ; plot data points
  ;  (add-pt current-state :points-fit line-color1)
  ;  (add-pt current-state :points-behavior line-color2)
  ;  (add-pt current-state :average-error line-color3)
  ;  (add-pt current-state :lowest-size line-color4)

    ; print to console
    (println "------------------------------------")
    (println (str "        Report for Generation" generation))

    (println "------------------------------------")
    (print "Best program: ")
    (println (best-overall-fitness population))
    (print "Best size: ")
    (println (lowest-size population))
    (print "Best total fitness: " )
    (println (best-fit population))
    (print "Average population error: ")
    (println (average-error population))
    (print "Best errors: ")
    (println "Errors here")) ; )

(defn population-has-solution
  "Returns true if population has a program with zero error.
   False otherwise."
  [population]
  (not (empty? (filter zero? (map :total-error population)))))

(defn find-or-stop
  "Returns either the first element of lst for which p returns
   true, or returns nil"
  [p max-elems lst]
  (loop [remaining max-elems
         l lst]
    (if (or (zero? remaining) (empty? l))
      nil
      (let [v (first l)]
        (if (p v)
          v
          (recur (dec remaining) (rest l)))))))

(defn make-generations
  "Returns a lazily-evaluated, Aristotelian infinite list
   representing all countable generations."
  [population-size instrs literals max-initial-program-size]
  (iterate
    (fn [population]
      (repeatedly
        population-size
        #(select-and-vary population)))
    (repeatedly
      population-size
      #(new-individual
        (generate-random-program
          instrs
          literals
          max-initial-program-size)))))

(defn foreach
  "Applies a function to each element of a and its index. Returns the col."
  [f col]
  (map-indexed f col)
  col)

(defn push-gp
  "Main GP loop. Initializes the population, and then repeatedly
  generates and evaluates new populations. Stops and returns :SUCCESS
  if it finds an individual with 0 error, or if it exceeds the maximum
  generations it returns nil. Print reports each generation.
  --
  Takes one argument: a map containing the core parameters to
  push-gp. The map's keys should include:
   - population-size
   - max-generations
   - testcases (a list of test cases)
   - instructions (a list of instructions)
   - literals (a list of literals)
   - number-inputs (the number of inputs the program will take)
   - max-initial-program-size (max size of randomly generated programs)"
  [{:keys [population-size max-generations testcases error-function instructions number-inputs literals max-initial-program-size]}]
  (let [all-inputs (map make-input-instruction (map inc (range number-inputs)))
        all-instrs (concat all-inputs instructions)
        target-gen (find-or-stop
                      population-has-solution
                      max-generations
                      (foreach #(report %2 %1)
                        (map #(map (fn [x] (run-tests x testcases)) %)
                        (make-generations
                          population-size
                          all-instrs
                          literals
                          max-initial-program-size))))]
    (if (nil? target-gen) nil :SUCCESS)))

