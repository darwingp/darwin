(ns push307.plush.generation
  (:require [push307.pushgp.utilities :as utils])
  (:gen-class))

(defn generate-random-genome
  "Creates and returns a new genome (note: not individual).
   Takes a list of instructions and a maximum initial program size."
  [instructions literals percent-literals max-size min-size]
  (repeatedly
    (+ (Math/round (* (- max-size min-size) (rand))) min-size)
    #(gene-wrap (utils/binary-rand-nth percent-literals literals instructions))))
