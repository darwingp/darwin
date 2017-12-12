(ns darwin.problems.pathfinding.instructions
  (:require [darwin.push.utilities :refer :all])
  (:gen-class))

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

(definstr loop_moves_2 [:integer :move :move :move] :move
  #(list "loop" %1 (list %2 %3 %4)))

(definstr while_moves [:integer :integer] :exec
  (fn [x y]
    (makemultipleinstr :move x :move
      (fn [& moves]
        (list "move-while" y moves)))))

(definstr while_moves_2 [:integer :move :move :move] :move
  #(list "move-while" %1 (list %2 %3 %4)))

(definstr move-dup [:integer :move] :exec
  #(vec (repeat %1 %2)))
