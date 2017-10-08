(ns push307.pushgp
  (:require [push307.push.instructions :refer [ins]])
  (:require [push307.push.utilities :refer :all])
  (:require [push307.pushgp.utilities :refer :all])
  (:require [push307.pushgp.crossover :refer :all])
  (:require [push307.pushgp.selection :refer :all])
  (:require [push307.pushgp.mutation :refer :all])
  (:require [push307.pushgp.generation :refer :all])
  (:require [push307.plotter :refer :all])
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

(def epsilon-percent 0.05)
(def epsilon-pool-size 10)

;; TODO: I think there's a bug here,
;;       it errors out after printing the first generation...
(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. Gives 50% chance to crossover,
  25% to uniform-addition, and 25% to uniform-deletion."
  [instructions literals population]
  (new-individual
  (let [v (rand-int 100)]
    (cond
;      (< v 50) (uniform-crossover
;                 (:program (epsilon-lexicase-selection population epsilon-pool-size epsilon-percent))
;                 (:program (epsilon-lexicase-selection population epsilon-pool-size epsilon-percent)))
;      (< v 75) (uniform-addition instructions
;                 (:program (epsilon-lexicase-selection population epsilon-pool-size epsilon-percent)))
;      :else    (uniform-deletion
;                 (:program (epsilon-lexicase-selection population epsilon-pool-size epsilon-percent)))
      (< v 60) (uniform-crossover
                  (:program (tournament-selection population 30))
                  (:program (tournament-selection population 30)))
      (< v 70) (uniform-deletion
                 (:program (tournament-selection population 30)))
      (< v 80) (uniform-addition instructions
                 (:program (tournament-selection population 30)))
      :else (uniform-mutation
                    instructions (range 3) 0.05
                    (:program (tournament-selection population 30)))
))))

(def indiv-error
  (fn [x] (:total-error x)))

(defn best-fit
  "takes population and determines best function fitness"
  [population]
  (reduce min (map indiv-error population)))

(defn median
  [numbers]
  (if (empty? numbers) nil
    (nth numbers (quot (count numbers) 2))))

(defn average
  [numbers]
    (quot (apply + numbers) (count numbers)))

(defn best-n-errors
  "returns lowest n errors in population"
  [pop n]
  (take n (sort (map overall-error pop)))
)

(defn lowest-size
  "Returns the length of the shortest program in a population of indivudals"
  [population]
  ;returns value 0-100
  (apply min
    (map count
      (map :program population))))

(defn fill-state
  "takes population and creates list of values"
  [pop gen]
  { :points-fit  (best-fit pop)
 ;;   :points-behavior (behavior-diversity pop)
    :average-error (average (map overall-error pop))
    :best-size (count (:program (best-overall-fitness pop)))
    :generation gen })

(defn report
  "Reports information on each generation."
  [population generation]
   (let [current-state (fill-state population generation)
         best (best-overall-fitness population)]
    ; plot data points
    (add-pt current-state :points-fit line-color-1)
  ;  (add-pt current-state :points-behavior line-color-2)
  ;  (add-pt current-state :average-error line-color-3)
    (add-pt current-state :best-size line-color-4)

    ; print to console
    (println "------------------------------------")
    (println (str "        Report for Generation " generation))
    (println "------------------------------------")
    (print "Best program: ")
    (println (:program best))
    (print "Best program errors: ")
    (println (:errors best))
    (print "Best program size: ")
    (println (count (:program best)))
    (print "Best total fitness: " )
    (println (double (:total-error best)))
    (print "Best 20 errors: ")
    (println (best-n-errors population 20))))

(defn population-has-solution
  "Returns true if population has a program with zero error.
   False otherwise."
  [population]
  (not (empty? (filter zero? (map :total-error population)))))

(defn make-generations
  "Returns a lazily-evaluated, Aristotelian infinite list
   representing all countable generations."
  [population-size instrs literals max-initial-program-size]
  (iterate
    (fn [population]
      (repeatedly
        population-size
        #(select-and-vary instrs literals population)))
    (repeatedly
      population-size
      #(new-individual
        (generate-random-program
          instrs
          literals
          max-initial-program-size)))))

(defn findl
  "Finds the first element in l for which p is true"
  [p l]
  (first (drop-while #(not (p %)) l)))

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
  (start-plotter)
  (let [all-inputs (take number-inputs ins)
        all-instrs (concat all-inputs instructions)
        gens (take
               max-generations
               (make-generations
                 population-size
                 all-instrs
                 literals
                 max-initial-program-size))
        tested-gens (map #(map (fn [x] (run-tests x testcases)) %) gens)
        trace (fn [idx v] (report v (inc idx)) v)
        solution-or-empty #(or (population-has-solution %) (findl (fn [i] (empty? (:program i))) %))
        result (findl population-has-solution (map-indexed trace tested-gens))]
    (if (nil? result) nil :SUCCESS)))
