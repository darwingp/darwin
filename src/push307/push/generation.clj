(ns push307.push.generation
  (:require [push307.pushgp.utilities :as utils])
  (:gen-class))

(defn generate-random-program
  "Creates and returns a new program (note: not individual).
   Takes a list of instructions and a maximum initial program size."
  [instructions literals percent-literals max-size min-size]
  (repeatedly
    (+ (Math/round (* (- max-size min-size) (rand))) min-size)
    #(utils/binary-rand-nth percent-literals literals instructions)))
