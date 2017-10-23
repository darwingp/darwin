(ns darwin.gp.testcases
  (:require [darwin.gp.utilities :refer :all])
  (:gen-class))

;; Linear Regression Testcases

(defn abs [n] (max n (- n)))

(defn target-function
  "The target function: f(x) = x^3 + x + 3"
  [x]
  (+' (+' (reduce *' (repeat 3 x)) x) 3))

(def penalty (reduce *' (repeat 20 (bigint 1000))))

(defn delta-error
  "Creates a function for measuring the difference between
   a single integer return value of a program and a function f."
  [f]
  (fn [inputs ret-state]
    (let [ints (:integer ret-state)]
      (if (empty? ints)
        penalty
        (abs (- (apply f inputs) (first ints)))))))

;;;;;;;;;;
;; Testcases are functions that take a push program
;; and return an error value. They are defined through
;; the testcase macro.
(def all
  (map #(make-testcase [%] (delta-error target-function)) (take 10 (range))))
