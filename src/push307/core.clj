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
            :testcases testcases/all
            :max-generations 500
            :population-size 200
            :max-initial-program-size 50}))
