(ns push307.pushgp.testcases
  (:require [push307.pushgp.utilities :refer :all])
  (:gen-class))

(defn target-function
  "The target function: f(x) = x^3 + x + 3"
  [x]
  (+ (+ (reduce * (repeat 3 x)) x) 3))

(defn delta-error
  "Creates a function for measuring the difference between
   a single integer return value of a program and a function f."
  [f]
  (fn [inputs ret-state] 
    (let [ints (:integer ret-state)]
      (if (empty? ints)
        (reduce * (repeat 20 (bigint 1000))) ;; REALLY large error
        (Math/abs (- (apply f inputs) (first ints)))))))

(testcase tf-one [1] (delta-error target-function))
(testcase tf-two [2] (delta-error target-function))

(def all [tf-one tf-two])
