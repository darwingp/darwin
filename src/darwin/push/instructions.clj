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
    (println "average: " (/ (reduce + test) (count test)))
    (println (reduce max test)))))

;move instruction generation
(definstr new_move [] :move (fn [] "angle 0"))
(definstr new_rand_angle [] :move (fn [] (str "angle " (angle-noise 45))))
(definstr new_angle [:integer] :move (fn [i] (str "angle " i)))
;TODO: this can be any number of moves: if-obs-range <range> angle <angle> angle <angle> ...angle <angle>
(definstr new_cond_moves [:integer :move :move :move :move] :move
  (fn [range & moves] (str "if-obs-range " range moves)))

(definstr set_angle_target [] :move "set-angle-target")

;TODO: multiple moves
(definstr loop_moves [:integer :move :move] (fn [i & moves] (str "loop " i moves))) ;add moves here

;TODO: multiple moves
(definstr while_moves [:integer :integer :move :move] (fn [i & moves] (str "move-while " i moves)))

(definstr move-dup [:integer :move] :move
  (fn [x mv] (repeat x mv)))

;advanced push instructions
(definstr integer-dup [:integer] :integer
  (fn [x] [x x]))

(definstr integer-frombool [:boolean] :integer
  (fn [x] (if x 1 0)))

(definstr boolean-and [:boolean :boolean] :boolean
  (fn [x y] (and x y)))

(definstr boolean-or [:boolean :boolean] :boolean
  (fn [x y] (or x y)))

(definstr exec-if [:exec :exec :boolean] :exec
  (fn [x y b] (if b x y)))

(definstr exec-dup [:exec] :exec
  (fn [x] [x x]))
