(ns darwin.push.instructions
  (:require [darwin.push.utilities :refer :all])
  (:gen-class))

(definstr integer_+ [:integer :integer] :integer +')
(definstr integer_- [:integer :integer] :integer -')
(definstr integer_* [:integer :integer] :integer *')

;; Protected division, returns numerator if the denominator is zero.
(definstr integer_% [:integer :integer] :integer
  (fn [x y] (if (zero? y) x (quot x y))))

(definstr integer_> [:integer :integer] :boolean >)
(definstr integer_< [:integer :integer] :boolean <)

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
