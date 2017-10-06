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
      (or
        (zero? (count tprime))
        (<= (count s) 1))
      (random-choice s)
      (let [lowert (random-choice tprime)
            elite (best-fitness-in s lowert)
            elite-error (error-on-test elite lowert)]
        (recur
          (filter #(not (= lowert %)) tprime)
          (filter #(comparator
                     (error-on-test % lowert)
                     elite-error) s))))))

(defn within-epsilon
  "Implements the epsilon part of epsilon-lexicase"
  [x elite]
  (let [deviation (- 1.0 (/ (float (overall-error elite)) (float (overall-error x))))]
    (< deviation 0.5)))

;; SELECTION OPERATORS

(defn tournament-selection
  "Selects an individual from the population using a tournament. Returned
  individual will be a parent in the next generation. Can use a fixed
  tournament size."
  [population number-to-select]
  (best-overall-fitness 
    (repeatedly
      number-to-select
      #(random-choice population))))

(defn -lexicase-selection
  "Performs lexicase selection on a population."
  [population number-to-select f]
  (best-overall-fitness (repeatedly
    number-to-select
    #(get-parent f population)))

(defn lexicase-selection
  [population number-to-select]
  (-lexicase-selection population number-to-select =))

(defn epsilon-lexicase-selection
  [population number-to-select]
  (-lexicase-selection population number-to-select within-epsilon))

