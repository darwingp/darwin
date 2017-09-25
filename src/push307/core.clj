(ns push307.core
  (:require [push307.pushgp :refer :all])
  (:require [push307.pushgp.testcases :as testcases])
  (:require [push307.pushgp.utilities :as utilities])
  (:gen-class))

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (push-gp {:instructions instructions
            :error-function #(utilities/run-tests % testcases/all)
            :max-generations 500
            :population-size 200
            :max-initial-program-size 50}))
