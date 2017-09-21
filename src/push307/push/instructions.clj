(ns push307.push.instructions
  (:require [push307.push.utilities :refer :all])
  (:gen-class))

(def in1
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack.
  Can't use make-push-instruction, since :input isn't a stack, but a map."
  (fn [state] (cons (:in1 (:input state)) (:exec state))))

(def in2
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack.
  Can't use make-push-instruction, since :input isn't a stack, but a map."
  (fn [state] (cons (:in2 (:input state)) (:exec state))))

(defmacro definstr
  "Macro for defining Push instructions."
  [name arg-stacks outputstack operation]
  (list 'def name (list 'fn '[state]
                  (list 'make-push-instruction 'state operation 
                  arg-stacks outputstack))))

;(def instruction-sets
;  '(integer_+ +' :integer :integer :integer))

;map definstr across instruction-sets

(definstr integer_+ [:integer :integer] :integer +')
(definstr integer_- [:integer :integer] :integer -')
(definstr integer_* [:integer :integer] :integer *')
(definstr integer_% [:integer :integer] :integer
  (fn [x y] (if (zero? y) x (/ x y)))) ;; protected division, returns numerator if denominator is zero.

; Pushes true onto the boolean stack if the second
; item is greater than the top item, else false
(definstr integer_> [:integer :integer] :boolean (fn [x y] (> y x)))

; Pushes true onto the boolean stack if the second
; item is less than the top item, else false.
(definstr integer_< [:integer :integer] :boolean (fn [x y] (< y x)))

