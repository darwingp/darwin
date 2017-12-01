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

(defn angle-noise
  "returns gaussian noise for angle move instruction generation"
  ; CITE: https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
  ; DESC: The Box-Muller method for generating uniformly distributed random numbers
  [alignment]
  (let [u (rand) v (rand)]
    (Math/round
    (* alignment
     (* (Math/sqrt (* -2 (Math/log u)))
        (Math/cos (* 2 Math/PI v)))))))

(defn noise-test
  [total]
  (let [test (repeatedly total (fn [] (angle-noise 45))) ]
    (do
    (println "average: " (/ (reduce +' test) (count test)))
    (println (reduce max test)))))

(def prep-moves
  (fn [& moves]
    (reduce (fn [total new] (str total " " new)) moves)))

;move instruction generation
(definstr new_move [] :move
  (constantly "angle 0 "))

(definstr new_angle [:integer] :move
  #(str "angle " % " "))

(definstr test_macro [:integer :integer :integer] :move
    #(str "move-while " %1 " angle " %2 " angle " %3 " "))

(definstr set_speed [:integer] :move
  #(str "set-speed " % " "))

(definstr new_cond_moves [:integer :integer] :exec
  (fn [x y] (makemultipleinstr :move x :move
    (fn [& moves] (str "if-obs-range " y " " (prep-moves moves) " ")))))

(definstr set_angle_target [] :move
  (constantly "set-angle-target "))

(definstr loop_moves [:integer :integer] :exec
  (fn [x y] (makemultipleinstr :move x :move
    (fn [& moves] (str "loop " y " " (prep-moves moves) " ")))))

(definstr while_moves [:integer :integer] :exec
  (fn [x y] (makemultipleinstr :move x :move
    (fn [& moves] (str "move-while " y " " (prep-moves moves) " ")))))

(definstr move-dup [:integer :move] :exec
  (fn [x mv] (repeat x mv)))

;advanced push instructions
(definstr integer-dup [:integer] :integer
  (fn [x] [x x]))

(definstr integer-frombool [:boolean] :integer
  #(if % 1 0))

(definstr boolean-and [:boolean :boolean] :boolean #(and % %2))
(definstr boolean-or [:boolean :boolean] :boolean #(or % %2))

(definstr exec-if [:exec :exec :boolean] :exec
  (fn [x y b] (if b x y)))

(definstr exec-dup [:exec] :exec
  (fn [x] [x x]))
