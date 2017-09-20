(ns push307.core
  (:require [push307.push :refer :all])
  (:require [push307.push.utilities :refer :all])
  (:require [push307.push.instructions :refer :all])
  (:require [push307.pushgp :refer :all])
  (:gen-class))



;;;;;;;;;;
;; The main function. Uses some problem-specific functions.

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (push-gp {:instructions instructions
            :error-function regression-error-function
            :max-generations 500
            :population-size 200
            :max-initial-program-size 50}))
