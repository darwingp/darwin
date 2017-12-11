(ns darwin.gp.mutation
  (:require [darwin.gp.utilities :refer :all])
  (:gen-class))

(defn uniform-deletion
  "Randomly deletes instructions/genes from a program/genome at some rate.
   Returns child program/genome."
  [delete-percent prog]
  (filter
    (fn [_] (not (true-percent? delete-percent)))
    prog))

(defn uniform-addition
  "Randomly adds new instructions/genes to an input program/genome
   before every instruction (and at the end) with some probability.
   Returns child program/genome."
  [new-element add-percent program]
  (reduce
    #(if (true-percent? add-percent)
      (concat %1 (list (new-element) %2))
      (concat %1 (list %2)))
    (list)
    program))

(defn uniform-mutation
  "Has an n percent chance of replacing each instruction in a program with a
   random instruction."
  [new-element mutate-percent program]
  (map
    #(if (true-percent? mutate-percent)
      (new-element)
       %)
    program))

