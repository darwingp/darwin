(ns darwin.core
  (:require [darwin.gp :refer :all])
  (:require [darwin.gp.testcases :as testcases])
  (:require [darwin.push.instructions :as instructions])
  (:gen-class))

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (run-gp {:genomic true
           :instructions instructions/all
           :literals '(1 2 3 4)
           :testcases testcases/all
           :number-inputs 1
           :max-generations 500
           :population-size 200
           :initial-percent-literals 0.2
           :max-initial-program-size 50
           :min-initial-program-size 10}))
