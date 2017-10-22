(ns push307.pushgp
  (:require [push307.push.instructions :refer [ins]])
  (:require [push307.push.utilities :refer :all])
  (:require [push307.pushgp.utilities :refer :all])
  (:require [push307.pushgp.crossover :refer :all])
  (:require [push307.pushgp.selection :refer :all])
  (:require [push307.pushgp.mutation :refer :all])
  (:require [push307.push.generation :refer :all])
  (:require [push307.graphics.plotter :refer :all])
  (:gen-class))

;parameters
;; TODO: these need to be moved into the GP parameters in core.clj
;; JACK: why did you use this? Why not just use the literals?
;(def literal-range (range 6))

(def literal-add% 0.15) ; JACK: shouldn't this just be :initial-percent-literals
                        ;       from the GP parameters?

;; TODO: move into run-gp parameters
(def event-percentage-add 7)
(def event-percentage-mutate 7)
(def event-percentage-del 7)

;; TODO: literal% and mutation operators
(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. Gives 50% chance to crossover,
  25% to uniform-addition, and 25% to uniform-deletion."
  [instructions literals population]
  (new-individual
  (let [v (rand-int 100)]
    ;percentage weights for various combinations expressed as conditional
    (cond
      (< v 60) (uniform-crossover
                 (:program (tournament-selection population 30))
                 (:program (tournament-selection population 30)))
      (< v 70) (uniform-deletion
                 event-percentage-del
                 (:program (tournament-selection population 30)))
      (< v 80) (uniform-addition
                 instructions
                 literals
                 literal-add%
                 event-percentage-add
                 (:program (tournament-selection population 30)))
      :else (uniform-mutation
              instructions
              literals ; literal-range
              literal-add%
              event-percentage-mutate
              (:program (tournament-selection population 30)))
))))

(defn best-fit
  "takes population and determines best function fitness"
  [population]
  (reduce min (map :total-error population)))

(defn median
  "return median"
  [numbers]
  (if (empty? numbers) nil
    (nth numbers (quot (count numbers) 2))))

(defn average
  "return average"
  [numbers]
    (quot (apply + numbers) (count numbers)))

(defn best-n-errors
  "returns lowest n errors in population"
  [pop n]
  (take n (sort (map :total-error pop)))
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
    :average-fitness (best-fit pop)
    :best-size (count (:program (best-overall-fitness pop)))
    :generation gen })

(defn print-many-ln
  "Prints args to stdout in a user-friendly way."
  [& args]
  (println (apply str (map print-str args))))

(defn report
  "Reports information on a generation."
  [population generation-num]
   (let [current-state (fill-state population generation-num)
         best (best-overall-fitness population)]
    ;; plot data points
    (add-pt current-state :points-fit line-color-1)
    ; (add-pt current-state :points-behavior line-color-2)
    (add-pt current-state :average-fitness line-color-3)
    (add-pt current-state :best-size line-color-4)

    ;; print stats to the console
    (print-many-ln "------------------------------------")
    (print-many-ln "        Report for Generation " generation-num)
    (print-many-ln "------------------------------------")
    (print-many-ln "Best individual: " (:program best))
    (print-many-ln " -> errors: " (:errors best))
    (print-many-ln " -> total error: " (:total-error best))
    (print-many-ln " -> size: " (count (:program best)))
    (print-many-ln "Best 20 errors: " (best-n-errors population 20))))

(defn population-has-solution
  "Returns true if population has a program with zero error.
   False otherwise."
  [population]
  (not (nil? (find-list zero? (map :total-error population)))))

(defn make-generations
  "Returns a lazily-evaluated, Aristotelian infinite list
   representing all countable generations."
  [population-size
   instrs
   literals
   percent-literals
   max-initial-program-size
   min-initial-program-size]
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
          percent-literals
          max-initial-program-size
          min-initial-program-size)))))

(defn run-gp
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
   - max-initial-program-size (max size of randomly generated programs)
   - min-initial-program-size (minimum size of randomly generated programs)
   - initial-percent-literals (how much of randomly generated programs should be literals, a float from 0.0 to 1.0)"
  [{:keys [population-size max-generations testcases error-function instructions number-inputs literals max-initial-program-size min-initial-program-size initial-percent-literals]}]
  (start-plotter)
  (let [all-inputs (take number-inputs ins) ; generate in1, in2, in3, ...
        gens (take
               max-generations
               (make-generations
                 population-size
                 (concat all-inputs instructions)
                 literals ;; THINK ON: should inputs be considered literals
                          ;; or instructions?
                 initial-percent-literals
                 max-initial-program-size
                 min-initial-program-size))
        prepared-gens (map #(map prepare-individual %) gens)
        tested-gens (map #(map (fn [x] (run-tests x testcases)) %) prepared-gens)
        result (find-list
                 population-has-solution
                 (map-indexed
                   #(do
                     (report %2 (inc %1))
                     %2)
                   tested-gens))]
    (if (nil? result) nil :SUCCESS)))
