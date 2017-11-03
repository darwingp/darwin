(ns darwin.problems.symbolicregression
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.utilities :as utils])
  (:gen-class))

(def penalty (reduce *' (repeat 20 (bigint 1000))))

(defn target-function
  "The target function: f(x) = x^3 + x + 3"
  [x]
  (+' (+' (reduce *' (repeat 3 x)) x) 3))

(defn delta-error
  "For a final Push state, compares (target-function in1) to 
   the top of the integer stack on that same Push state and returns
   the difference, always positive."
  [ret-state]
  (let [ints (:integer ret-state)
        x (first (vals (into (sorted-map) (:input ret-state))))]
    (if (empty? ints)
      penalty
      (Math/abs (- (target-function x) (first ints))))))

(def instructions
  '(integer_+
    integer_-
    integer_*
    integer_%))

(def configuration
  {:genomic true
   :instructions instructions
   :literals '(1 2 3 4)
   :inputses (map list (map #(+ 2 %) (range 10)))
   :program-arity 1
   :testcases (list delta-error)
   :max-generations 500
   :population-size 200
   :initial-percent-literals 0.2
   :max-initial-program-size 50
   :min-initial-program-size 10
   :evolution-config {:selection #(selection/lexicase-selection % 30)
                      :crossover crossover/uniform-crossover
                      :percentages '([60 :crossover]
                                     [10 :deletion]
                                     [10 :addition]
                                     [20 :mutation])
                      :deletion-percent 7
                      :addition-percent 7
                      :mutation-percent 7
                      }})
