(ns push307.push.instructions
  (:require [push307.push.utilities :refer :all])
  (:gen-class))

;; CITE: http://faculty.hampshire.edu/lspector/push3-description.html#Type
;; DESC:
;; Behavior implemented according to document at URL ^
;; Push functions come after their arguments.
;; Given the following Push expression: (arg1 arg2 func)
;;   1. exec: (arg1 arg2 func)
;;   2. exec: arg1 arg2 func
;;   3. exec: arg2 func
;;      arg: arg1
;;   4. exec: func
;;      arg: arg2 arg1
;;   Finally in LISP land (definstr):
;;     (func arg1 arg2)

;; An infinite list representing inputs in1...in<n>.
;; The list itself is symbols, and each element has a side
;; effect of defining the necessary function for it to work.
(def ins (map (fn [i]
  (let [sym (symbol (str "in" i))]
    (intern 'push307.push.instructions sym (make-input-instruction i))
    sym))
 (map inc (range))))

(definstr integer_+ [:integer :integer] :integer +')
(definstr integer_- [:integer :integer] :integer -')
(definstr integer_* [:integer :integer] :integer *')

;; Protected division, returns numerator if the denominator is zero.
(definstr integer_% [:integer :integer] :integer
  (fn [x y] (if (zero? y) x (int (quot x y)))))

(definstr integer_> [:integer :integer] :boolean >)
(definstr integer_< [:integer :integer] :boolean <)

(def all
  '(integer_+
    integer_-
    integer_*
    integer_%))

