(ns push307.pushgp.selection
  (:require [push307.pushgp.testcases :refer :all])
  (:gen-class))

;; HELPERS

(defn random-choice
  "Selects an element in a collection by random"
  [coll]
  (let [idx (inc (rand-int (dec (count coll))))]
    (nth coll idx)))

(defn best-fitness-in
  "Returns the most fit individual in the population given a test"
  [population test]
  (:program (reduce
    (fn [acc x]
      (if (fitness-gt (:fitness acc) (fitness x test))
      acc
      { :fitness (fitness x) :program x }))
    {} population)))

(defn get-parent
  "Gets a parent for lexicase-selection"
  [comparator population training-cases]
  (loop [tprime training-cases
         s population]
    (if
      (not
        (and
          (zero? (count tprime))
          (> (count s) 1)))
      (random-choice s)
      (let [lowert (random-choice tprime)
            elite (best-fitness-in s lowert)]
        (recur
          (remove #(= lowert %) tprime)
          (filter #(comparator (fitness % lowert) elite) s))))))

(defn epsilon
  "Implements the epsilon part of epsilon-lexicase"
  [x elite]
  :STUB)

;; SELECTION OPERATORS

(defn tournament-selection
  "Selects an individual from the population using a tournament. Returned
  individual will be a parent in the next generation. Can use a fixed
  tournament size."
  ;epsilon
  [population tests]
  (let [subpop (map #(apply % '()) (repeat 5 (fn [] (random-choice population))))]
    (best-fitness-in subpop (concat-tests tests))))

(defn lexicase-selection
  "Performs lexicase selection on a population"
  [population training-cases number-to-select]
  (map
    (fn [] (get-parent = population training-cases))
    (range number-to-select)))

