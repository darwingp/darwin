(ns push307.pushgp.selection
  (:require [push307.pushgp.utilities :refer :all])
  (:gen-class))

;; HELPERS

(defn get-parent
  "Gets a parent for lexicase-selection"
  [comparator population]
  (loop [tprime (range (number-tests population))
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
          (filter #(comparator (error-on-test % lowert) elite) s))))))

(defn within-epsilon
  "Implements the epsilon part of epsilon-lexicase"
  [x elite]
  :STUB)

;; SELECTION OPERATORS

(defn tournament-selection
  "Selects an individual from the population using a tournament. Returned
  individual will be a parent in the next generation. Can use a fixed
  tournament size."
  [population number-to-select]
  (let [subpop (map #(%)
                    (repeat
                      number-to-select
                      (fn [] (random-choice population))))]
    (best-overall-fitness subpop)))

(defn -lexicase-selection
  "Performs lexicase selection on a population."
  [population number-to-select f]
  (map
    (fn [] (get-parent f population))
    (range number-to-select)))

(defn lexicase-selection
  [population number-to-select]
  (-lexicase-selection population number-to-select =))

(defn epsilon-lexicase-selection
  [population number-to-select]
  (-lexicase-selection population number-to-select within-epsilon))

