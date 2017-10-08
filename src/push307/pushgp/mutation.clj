(ns push307.pushgp.mutation
  (:require [push307.pushgp.utilities :refer :all])
  (:gen-class))

(def event-percentage 5)

(defn uniform-deletion
  "Randomly deletes instructions from program at some rate. Returns child program."
  [prog]
  (filter
    (fn [_] (true-percent? (- 100 event-percentage)))
    prog))

(defn uniform-addition
  "Randomly adds new instructions before every instruction
   (and at the end of the program) with some probability.
   Returns child program."
  [instructions program]
  (reduce
    #(if (true-percent? event-percentage)
      (conj (conj %1 (random-choice instructions)) %2)
      (conj %1 %2))
    (list)
    program))

(defn uniform-mutation
  "Has an n percent chance of replacing each instruction in a program with a
   random instruction."
  [instructions program]
  (map
    #(if (true-percent? event-percentage)
       (random-choice instructions)
       %)
    program))

