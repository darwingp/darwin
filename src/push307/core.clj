(ns push307.core
  (:require [push307.pushgp :refer :all])
  (:require [push307.pushgp.testcases :as testcases])
  (:require [push307.push.instructions :as instructions])
  (:gen-class))

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (push-gp {:instructions instructions/all
            :literals '(1 2 3 4)
            :testcases testcases/all
            :number-inputs 1
            :max-generations 500
            :population-size 200
            :max-initial-program-size 50}))
