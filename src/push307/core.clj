(ns push307.core
  (:require [push307.pushgp :refer :all])
  (:require [push307.pushgp.testcases :as testcases])
  (:require [push307.push.instructions :as instructions])
  (:require [push307.plotter :refer :all])
  (:gen-class))

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (start-plotter)
  (push-gp {:instructions instructions/all
            :literals (map inc (range 10))
            :testcases testcases/all
            :number-inputs 1
            :max-generations 5
            :population-size 2
            :max-initial-program-size 50}))
