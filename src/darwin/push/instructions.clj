(ns darwin.push.instructions
  (:require [darwin.push.utilities :refer :all])
  (:gen-class))

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
  (fn [x y] (if (zero? y) x (quot x y))))

(definstr integer_> [:integer :integer] :boolean >)
(definstr integer_< [:integer :integer] :boolean <)

;advanced push instructions
(definstr integer-dup [:integer] :integer
  (fn [x] [x x]))

(definstr integer-frombool [:boolean] :integer
  #(if % 1 0))

(definstr boolean-and [:boolean :boolean] :boolean #(and % %2))
(definstr boolean-or [:boolean :boolean] :boolean #(or % %2))

(definstr exec-if [:boolean :exec :exec] :exec
  (fn [b x y] (if b x y)))

(definstr exec-dup [:exec] :exec
  (fn [x] [x x]))
