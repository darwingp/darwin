(ns push307.pushgp.selection
  (:require [push307.pushgp.utilities :refer :all])
  (:gen-class))

;; FIXME: Something isn't quite right here
;;        the base case of the loop/recursion isn't quite right.
(defn get-parent
  "Gets a parent for 'lexicase-selection` from 'population`.
   Errors are compared using 'comparator` instead of merely equality."
  [comparator population]
  (loop [tprime (range (number-tests population))
         s population]
    (cond
      (<= (count s) 1) (rand-nth s)
      (empty? tprime)  (rand-nth s)
      :else (let [test-idx (rand-nth tprime)
                  elite (best-fitness-in s test-idx)
                  elite-error (error-on-test elite test-idx)]
              (recur
                (filter #(not (= test-idx %)) tprime)
                (filter #(comparator (error-on-test % test-idx) elite-error) s))))))

(defn within-epsilon
  "Implements the epsilon part of epsilon-lexicase.
   Takes a percentage and returns a function that compares
   two individuals and returns whether the first individual's
   error is within 'ep-percent` of the other. 'ep-percent` is
   a float from 0.0 to 1.0."
  [ep-percent]
  (fn
    [x elite]
    (let [x-err (float (overall-error x))
          elite-err (float (overall-error elite))
          delta (- 1.0 (/ elite-err x-err))]
      (<= delta ep-percent))))

;; SELECTION OPERATORS

(defn tournament-selection
  "Selects an individual 'population` using a tournament. Returned individual
   will be a parent in the next generation. Uses a tournament size of 'number-to-select`."
  [population number-to-select]
  (best-overall-fitness
    (repeatedly
      number-to-select
      #(rand-nth population))))

(defn -lexicase-selection
  "Performs lexicase selection on a population."
  [population number-to-select f]
  (rand-nth (repeatedly
    number-to-select
    #(get-parent f population))))

(defn lexicase-selection
  "additional version"
  [population number-to-select]
  (-lexicase-selection population number-to-select =))

(defn epsilon-lexicase-selection
  "epsilon lexicase"
  [population number-to-select percent]
  (-lexicase-selection population number-to-select (within-epsilon percent)))
