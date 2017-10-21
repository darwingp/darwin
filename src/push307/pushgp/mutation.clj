(ns push307.pushgp.mutation
  (:require [push307.pushgp.utilities :refer :all])
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
  [instructions literals percent-literal add-percent program]
  (reduce
    #(if (true-percent? add-percent)
      (concat %1 (list (binary-rand-nth percent-literal literals instructions) %2))
      (concat %1 (list %2)))
    (list)
    program))

(defn uniform-addition-genome
  "Same as uniform-addition, but works on a plush genome instead of a push program."
  [instructions literals percent-literal add-percent genome]
  (uniform-addition
    (map gene-wrap instructions)
    (map gene-wrap literals)
    percent-literal
    add-percent
    genome))

(defn uniform-mutation
  "Has an n percent chance of replacing each instruction in a program with a
   random instruction."
  [instructions literals percent-literal mutate-percent program]
  (map
    #(if (true-percent? mutate-percent)
      (binary-rand-nth percent-literal literals instructions)
       %)
    program))

(defn uniform-mutation-genome
  "Same as uniform-mutation, but works on a genome."
  [instructions literals percent-literal mutate-percent genome]
  (uniform-mutation
    (map gene-wrap instructions)
    (map gene-wrap literals)
    percent-literal
    mutate-percent genome))
