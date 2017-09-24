(ns push307.push.instructions
  (:require [push307.push.utilities :refer :all])
  (:gen-class))

;; Behavior implemented according to
;; http://faculty.hampshire.edu/lspector/push3-description.html#Type
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

(def in1
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack.
  Can't use make-push-instruction, since :input isn't a stack, but a map."
  (fn [state] (cons (:in1 (:input state)) (:exec state))))

(def in2
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack.
  Can't use make-push-instruction, since :input isn't a stack, but a map."
  (fn [state] (cons (:in2 (:input state)) (:exec state))))

(definstr integer_+ [:integer :integer] :integer +')
(definstr integer_- [:integer :integer] :integer -')
(definstr integer_* [:integer :integer] :integer *')

;; Protected division, returns numerator if the denominator is zero.
(definstr integer_% [:integer :integer] :integer
  (fn [x y] (if (zero? y) x (/ x y))))

(definstr integer_> [:integer :integer] :boolean >)
(definstr integer_< [:integer :integer] :boolean <)

