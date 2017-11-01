(ns darwin.core
  (:require [darwin.gp :refer :all])
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.testcases :as testcases])
  (:require [darwin.push.instructions :as instructions])
  (:gen-class))

(def symbolic-regression
  {:genomic true
   :instructions instructions/all ;; TODO: rewrite this
   :literals '(1 2 3 4)
   :testcases testcases/all ;; TODO: rewrite this
   :number-inputs 1
   :max-generations 500
   :population-size 200
   :initial-percent-literals 0.2
   :max-initial-program-size 50
   :min-initial-program-size 10
   :evolution-config {:selection #(selection/tournament-selection % 30)
                      :crossover crossover/uniform-crossover
                      :percentages '([60 :crossover]
                                     [10 :deletion]
                                     [10 :addition]
                                     [20 :mutation])
                      :deletion-percent 7
                      :addition-percent 7
                      :mutation-percent 7
                      }})

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (run-gp symbolic-regression))
