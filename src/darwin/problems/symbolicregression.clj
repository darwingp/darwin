(ns darwin.problems.symbolicregression
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.mutation :as mutation])
  (:require [darwin.gp.utilities :as utils])
  (:require [darwin.gp.hotspots :as hotspots])
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
      (utils/abs (- (target-function x) (first ints))))))

(def instructions
  '(integer_+
    integer_-
    integer_*
    integer_%))

(def instruction-heat
  { 'integer_+ 3
    'integer_- 1
    'integer_* 6
    'integer_% 0
    'in1 3})

(def literal-heat
  { 3 4 })

(def configuration
  {:genomic true
   :instructions instructions
   :instruction-heat instruction-heat
   :literals '(1 2 3 4)
   :literal-heat literal-heat
   :inputses (map list (map #(+ 2 %) (range 10)))
   :program-arity 1
   :testcases (list delta-error)
   :max-generations 500
   :population-size 200
   :initial-percent-literals 0.2
   :max-initial-program-size 50
   :min-initial-program-size 10
   :evolution-config {:crossover (hotspots/wrap crossover/uniform-crossover)
                      :deletion #((hotspots/wrap 
                                   (fn [g] (mutation/uniform-deletion %1 g))) %2)
                      :mutation #((hotspots/wrap
                                   (fn [g] (mutation/uniform-mutation %1 %2 g))) %3)
                      :decrease-heat-by-age false
                      :percentages '([0 :crossover]
                                     [0 :deletion]
                                     [0 :addition]
                                     [100 :mutation])
                      :deletion-percent 7
                      :addition-percent 7
                      :mutation-percent 7
                      :end-action (fn [_] (println "Done"))
                      ;:keep-test-attribute false
                      }})
