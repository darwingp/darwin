(ns darwin.plush.generation
  (:require [darwin.gp.utilities :as utils])
  (:gen-class))

(defn generate-random-genome
  "Creates and returns a new genome (note: not individual).
   Takes a list of instructions and a maximum initial program size."
  [instructions literals percent-literals max-size min-size]
  (utils/prepare-individual
    {:genome 

 (repeatedly
       (+ (Math/round (* (- max-size min-size) (rand))) min-size)
       #(utils/gene-wrap
          (utils/binary-rand-nth percent-literals
                                 literals
                                 instructions)))
}))
