(ns push307.pushgp.mutation
  (:require [push307.pushgp.utilities :refer :all])
  (:gen-class))

(def event-percentage-add 7)
(def event-percentage-del 7)

(defn uniform-deletion
  "Randomly deletes instructions from program at some rate. Returns child program."
  [prog]
  (filter
    (fn [_] (not (true-percent? event-percentage-del))) ;; Don't delete 95% of the time
    prog))

(defn uniform-addition
  "Randomly adds new instructions before every instruction
   (and at the end of the program) with some probability.
   Returns child program."
  [instructions program]
  (reduce
    #(if (true-percent? event-percentage-add) ;; do an addition 5% of the time
      (concat %1 (list (random-choice instructions) %2))
      (concat %1 (list %2)))
    (list)
    program))

(defn uniform-mutation
  "Has an n percent chance of replacing each instruction in a program with a
   random instruction."
  [instructions program]
  (map
    #(if (true-percent? event-percentage-add) ; do a mutation 5% of the time
       (random-choice instructions)
       %)
    program))
