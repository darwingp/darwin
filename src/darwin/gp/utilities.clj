(ns darwin.gp.utilities
  (:require [darwin.push.utilities :refer :all])
  (:require [darwin.push :refer :all])
  (:require [darwin.plush.translate :refer :all])
  (:gen-class))

(defn find-list
  "Finds the first element in l for which p is true"
  [p l]
  (first (drop-while #(not (p %)) l)))

(defn binary-rand-nth
  "Returns a random member of collection 'a` 'bias-percent` of the
  time or a random member of collection 'b` the rest of the time.
  'bias-percent` is a float from 0.0 to 1.0"
  [bias-percent a b]
  (if (< (rand) bias-percent)
    (rand-nth a)
    (rand-nth b)))

(defn true-percent?
  "Returns true n percent of the time."
  [n]
  (<= (inc (rand-int 100)) n))

; The default individual
(def default-individual { :errors '()
                          :total-error 0
                          :program '() })

(defn prepare-individual
  "Prepares an individual for running/testing. Takes an individual
   and returns a copy of it that's ready for use. This involves
   checking for the presence of a :genome and setting :program accordingly."
  [ind]
  (if (not (nil? (:genome ind)))
    (merge ind default-individual { :program (translate-plush-genome-to-push-program (:genome ind)) })
    (merge ind default-individual)))

(defn make-testcase
  "Tests are functions that take a program and return an error.
   This function takes inputs which are used to construct an initial
   Push state with in1 etc set in increasing order, eg [in1 in2 in3]."
  [inputs error-fn]
  (let [state (assoc empty-push-state :input (mk-inputs inputs))]
    #(error-fn inputs (interpret-push-program % state))))

(defmacro testcase
  "Defines a testcase."
  [name inputs error-fn]
  (list 'def name (list 'make-testcase inputs error-fn)))

(defn run-tests
  "Runs each of an individual's tests, Returning an individual with
   :errors set to the errors encountered running tests on the
   individual. Then sums that and sets it to :total-error."
  [individual tests]
  (let [prog (:program individual)
        errors (map #(% prog) tests)]
        (merge individual {:errors errors
                                  :total-error (reduce +' errors) })))

(defn gene-wrap
  "Creates a gene given a value the gene represents."
  [v]
  { :value v })

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

(defn best-overall-fitness
  "Returns the member of the population with the the lowest total error."
  [population]
  (reduce
    #(if (< (:total-error %1) (:total-error %2)) %1 %2)
    population))

(defn number-tests
  "Returns the number of tests for a population."
  [pop]
  (count (:errors (rand-nth pop))))
