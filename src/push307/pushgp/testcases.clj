(ns push307.pushgp.testcases
  (:gen-class))

;; HELPERS

(defn concat-tests
  "Turns a list of tests into a single test, returning the average fitness."
  [tests]
  :STUB
)

;; What's a test case?
;; 1. function taking a program and returning an error?

;; RUNNING TESTS

(defn fitness
  "Determines the fitness of a program using a test"
  [program test]
  :STUB
  )

(defn avg-fitness
  "Determines the average fitness of a program using multiple tests"
  [program tests]
  (/ (map #(fitness program %) tests) (count tests)))

(def fitness-gt <)

;; TESTS THEMSELVES?
