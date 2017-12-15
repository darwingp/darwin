(ns darwin.gp.crossover
  (:require [darwin.utilities :refer :all])
  (:require [darwin.gp.utilities :refer :all])
  (:gen-class))

(defn truncate-lists
  "Returns a vector: [new-a new-b tail] where new-a and new-b are
   the the same length and tail is the remaining portion of either a or b."
  [a b]
  (let [len (min (count a) (count b))]
    [(take len a)
     (take len b)
     (drop len (if (> (count a) (count b)) a b))]))

;; Uniform Crossover

(defn uniform-crossover
  "Has an equal probability of selecting a gene/instruction/literal from either
   parent program when crossing them over. Takes two genomes or programs and
   return a genome or program."
  [a b]
  (let [[aa bb tail] (truncate-lists a b)]
    (concat (map #(if (= (rand-int 2) 1) %1 %2) aa bb) tail)))

;; Alternation Crossover

(defn add-noise
  "returns gaussian noise for alternation crossover index modification"
  ; CITE: https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
  ; DESC: The Box-Muller method for generating uniformly distributed random numbers
  [alignment]   ;standard deviation (usually around 10)
  (let [u (rand) v (rand)]
    (Math/round
    (* alignment
     (* (Math/sqrt (* -2 (Math/log u)))
        (Math/cos (* 2 Math/PI v)))))))

(defn alternation-crossover
  "Crosses over two programs or genomes (note: not individuals) using alternation crossover
  takes alternation rate and alignment-deviation"
  [prog-a prog-b alternation-rate alignment-deviation]
  (loop [index 0
         child '()
         pa prog-a
         pb prog-b]
    (if (or
          (>= (abs index) (count prog-a))
          (>= (abs index) (count prog-b)))
      (reverse child)
      (let [alternate (< (rand) alternation-rate)]
        (recur
         (+' index (if alternate (add-noise alignment-deviation) 1))
         (cons (nth pa (abs index)) child)
         (if alternate pb pa)
         (if alternate pa pb))))))
