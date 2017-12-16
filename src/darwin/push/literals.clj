(ns darwin.push.literals
  (:require [darwin.push.utilities :as utilities])
  (:gen-class))

(def ins
  "An infinite list representing inputs in1...in<n>.
   The members of the list are symbols, and each element has
   a side effect of defining itself inside the interpreter."
  (map
    #(:name (meta (intern
        'darwin.push.literals
        (symbol (str "in" %))
        (utilities/make-input-instruction %))))
    (map inc (range))))
