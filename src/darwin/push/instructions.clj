(ns darwin.push.instructions
  (:require [darwin.push.utilities :refer :all])
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
    (intern 'darwin.push.instructions sym (make-input-instruction i))
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

;TODO: figure out good distribution of 0-180 with more zeros/low #s than higher #s
(defn angle-noise
  "returns gaussian noise for angle move instruction generation"
  ; CITE: https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
  ; DESC: The Box-Muller method for generating uniformly distributed random numbers
  [alignment]
  (let [u (rand) v (rand)]
    (Math/round
    (* 180
     (* (Math/sqrt (* -2 (Math/log u)))
        (Math/cos (* 2 Math/PI v)))))))

;move instruction generation
(definstr new_move [] :move (fn [] "angle 0"))
(definstr new_angle [] :move (fn [] (str "angle " (rand-int 180))))
