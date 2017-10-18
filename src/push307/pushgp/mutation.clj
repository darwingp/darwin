(ns push307.pushgp.mutation
  (:require [push307.pushgp.utilities :refer :all])
  (:gen-class))

;percentage likelihood of addition or deletion event
(def event-percentage-add 7)
(def event-percentage-del 7)

(defn uniform-deletion
  "Randomly deletes instructions/genes from a program/genome at some rate.
   Returns child program/genome."
  [prog]
  (filter
    (fn [_] (not (true-percent? event-percentage-del))) ;; Don't delete 95% of the time
    prog))

;; TODO: how make new genes from instructions and literals?

;; TODO: make this use a percent-literal like uniform-mutation
(defn uniform-addition
  "Randomly adds new instructions/genes to an input program/genome
   before every instruction (and at the end) with some probability.
   Returns child program/genome."
  [instructions literals program]
  (let [instrs-n-lits (concat instructions literals)]
    (reduce
      #(if (true-percent? event-percentage-add) ;; do an addition 5% of the time
        (concat %1 (list (rand-nth instrs-n-lits) %2))
        (concat %1 (list %2)))
      (list)
      program)))

(defn uniform-mutation
  "Has an n percent chance of replacing each instruction in a program with a
   random instruction."
  [instructions literals percent-literal program]
  (map
    #(if (true-percent? event-percentage-add) ; do a mutation 5% of the time
      (if (< (rand) percent-literal)
       (rand-nth literals)
       (rand-nth instructions))
       %)
    program))
