(ns push307.pushgp.utilities
  (:require [push307.push.utilities :refer :all])
  (:require [push307.push :refer :all])
  (:gen-class))

;; TESTS
;; Tests are functions which take a program and return an error.

(defn make-testcase
  "Tests are functions that take a program and return an error.
   This function takes inputs which are used to construct an initial
   Push state with in1 etc set in increasing order, eg [in1 in2 in3]."
  [inputs error-fn]
  ;; TODO: use reduce!
  (let [new-state (loop [st empty-push-state
                         idx 1
                         is inputs]
                    (println (str "I: " idx))
                    (if
                      (empty? is)
                      st
                      (recur
                        (set-input st idx (first inputs))
                        (inc idx)
                        (rest inputs))))]
    (fn [program]
      (error-fn inputs (interpret-push-program program new-state)))))

(defmacro testcase
  "Defines a testcase."
  [name inputs error-fn]
  (list 'def name (list 'make-testcase inputs error-fn)))

(defn run-tests [individual tests]
  "Runs each of an individual's tests, Returning an individual with
   :errors set to the errors encountered running tests on the
   individual. Then sums that and sets it to :total-error."
  (let [program (:program individual)
        errors (map #(% program) tests)]
    { :errors errors
      :total-error (reduce +' errors)
      :program program }))

(defn new-individual [program]
  "Creates a blank individual from a push program in list form."
  { :program program :errors '() :total-error 0 })

(defn random-choice
  "Selects an element in a collection by random"
  [coll]
  (let [idx (inc (rand-int (dec (count coll))))]
    (nth coll idx)))

;; any time a test is mentioned, it's the idx in the individual.

(defn error-on-test
  "Returns the error an individual got on a test"
  [individual test-idx]
  (nth (:errors individual) test-idx))

(defn best-fitness-in
  "Returns the most fit individual in the population given a test"
  [population test-idx]
  (reduce
    #(if (< (error-on-test %1 test-idx) (error-on-test %2 test-idx)) %1 %2)
    population))

(defn overall-error
  [individual]
  (:total-error individual))

(defn best-overall-fitness
  [population]
  (reduce
    #(if (< (overall-error %1) (overall-error %2)) %1 %2)
    population))

(defn number-tests
  "Returns the number of tests for a population."
  [pop]
  (count (:errors (random-choice pop))))