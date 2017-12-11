(ns darwin.push.instructions
  (:require [darwin.push.utilities :refer :all])
  (:gen-class))

;; TODO: Return lists to the :move stack
;;       push interpreter misinterprets a list being returned

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
  (fn [x y] (if (zero? y) x (quot x y))))

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

;move instruction generation
(definstr new_move [] :move
  #(list "angle" 0))

(definstr new_angle [:integer] :move
  #(list "angle" %))

(definstr test_macro [:integer :integer] :move
  #(list "move-while" %1 (list
                           (list "angle" %2))))

(definstr test_macro_2 [:integer :integer :integer] :move
  #(list "loop" %1 (list
                     (list "angle" %2)
                     (list "move-while" 10
                       (list
                         (list "angle" %3))))))

(definstr test_macro_3 [:integer :integer] :move
  #(list "move-while" %1 (list
                           (list "angle" %2)
                           (list "loop" 10
                             (list
                               (list "angle" 45))))))

(definstr simple_loop [:integer :integer] :move
  #(list "loop" %1 (list (list "angle" %2))))

(definstr loop_compose [:move] :move
  #(list "loop" 10 (list %)))

(definstr set_speed [:integer] :move
  #(list "set-speed" %))

(definstr new_cond_moves [:integer :integer] :exec
  (fn [x y]
    (makemultipleinstr :move x :move
      (fn [& moves]
        (list "if-obs-range" y moves)))))

(definstr set_angle_target [] :move
  #(list "set-angle-target"))

(definstr loop_moves [:integer :integer] :exec
  (fn [x y]
    (makemultipleinstr :move x :move
      (fn [& moves]
        (list "loop" y moves)))))

;; FIXME: Is this correct??
(definstr loop_moves_2 [:integer :move :move :move] :move
  #(list "loop" %1 %2 %3 %4))

(definstr while_moves [:integer :integer] :exec
  (fn [x y]
    (makemultipleinstr :move x :move
      (fn [& moves]
        (list "move-while" y moves)))))

;; FIXME: is this correct??
(definstr while_moves_2 [:integer :move :move :move] :move
  #(list "move-while" %1 %2 %3 %4))

(definstr move-dup [:integer :move] :exec
  #(vec (repeat %1 %2)))

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
