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

;; TODO: enable this to push to multiple stacks.
;;  e.g. vectors/lists -> push multiple
;;       dictionaries -> push multiple on different stacks
;; You could redefine the meaning of these things as applied
;; here because you couldn't enforce type safety when pushing
;; dicts and lists to stacks, so Push wouldn't care if we
;; commandeered these types.
;; This would be needed to be abstract enough while
;; being able to implement  exec_* instructions & in*.
(defmacro definstr
  "Macro for defining Push instructions."
  [name arg-stacks outputstack operation]
  (list 'def name (list 'fn '[state]
                  (list 'make-push-instruction 'state operation 
                  arg-stacks outputstack))))

;; FIXME: make-push-instruction returns args backwards
;;        and this causes definstr's "typed function declaration"
;;        syntax to be misleading.
;; Flipping the order would fix integer_> and friends.
;; This makes more sense because it upholds the notational
;; order of Push.
;; 
;; This is proven by
;; ... define make-push-instruction & definstr
;; (definstr i [:integer :boolean] :exec (fn [i b] [i b]))
;; => #'user/i
;; (i { :integer '(1 2 3) :boolean '(true) })
;; => {:integer (2 3), :boolean (), :exec ([true 1])}
;; Options:
;; 1. s/arg-stacks/(reverse arg-stacks)/ on line 29.
;; 2. patch make-push-instruction
;;
;; Which course of action should be taken? I prefer option 1.

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

