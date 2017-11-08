(ns darwin.gp.selection
  (:require [darwin.gp.utilities :refer :all])
  (:gen-class))

(defn get-parent
  "Gets a parent for 'lexicase-selection` from 'population`.
   Errors are compared using 'comparator` instead of merely equality."
  [comparator population]
  (loop [tprime (range (count (:errors (first population))))
         s population]
    (cond
      (empty? tprime) (rand-nth s)
      :else (let [test-idx (rand-nth tprime)
                  elite (best-fitness-in s test-idx)
                  elite-error (error-on-test elite test-idx)]
              (recur
                (filter #(not (= test-idx %)) tprime)
                (filter
                  #(comparator
                    (error-on-test % test-idx)
                    elite-error)
                  s))))))

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
  [population number-to-select comparator]
  (rand-nth
    (repeatedly
      number-to-select
      #(get-parent comparator population))))

(defn lexicase-selection
  "additional version"
  [population number-to-select]
  (-lexicase-selection population number-to-select =))

(defn epsilon-lexicase-selection
  "epsilon lexicase"
  [population number-to-select epsilon]
  (-lexicase-selection
    population
    number-to-select
    #(<= (Math/abs (-' %2 %1)) epsilon)))
