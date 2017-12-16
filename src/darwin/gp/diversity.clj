(ns darwin.gp.diversity
  (:require [darwin.utilities :refer :all])
  (:gen-class))

;; TODO: rewrite using recur
(defn manhattan-distance
  [a b]
  (cond
    (or (keyword? a)
        (keyword? b)
        (symbol? a)
        (symbol? b)) (manhattan-distance (str a) (str b))
    (or
      (and (string? a)
           (string? b))
      (and (coll? a)
           (coll? b))) (reduce +' 0 (map manhattan-distance a b))
    (and (integer? a)
         (integer? b)) (abs (-' a b))
    (and (char? a)
         (char? b)) (manhattan-distance (int a) (int b))
    :else (if (= a b) 0 1)))

(defn generic
  "Calculate the diversity of a list of collections given a comparator."
  [colls comparator]
  (double
    (/
      (reduce +'
        (pmap
         #(reduce
           (fn [acc x] (reduce +' acc (map comparator % x)))
           0
           colls)
         colls))
      (max 1 (count colls)))))

(defn behavioral
  "Calculate the behavioral diversity of a population using a
   comparator."
  [population comparator]
  (generic (map :exit-states population) comparator))

(defn genomic
  "Calculate the genomic diversity of a population using a
   comparator."
  [population comparator]
  (generic (map :genome population) comparator))

(defn error
  "Calculate the diversity of a population using error, given a comparator."
  [population comparator]
  (generic (map :errors population) comparator))
