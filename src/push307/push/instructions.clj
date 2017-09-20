(ns push307.push.instructions
  (:gen-class))

(def in1
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack.
  Can't use make-push-instruction, since :input isn't a stack, but a map."
  (fn [state] (cons (:in1 (:input state)) (:exec state))))

(def in2
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack.
  Can't use make-push-instruction, since :input isn't a stack, but a map."
  (fn [state] (cons (:in2 (:input state)) (:exec state))))



;define instructions wil macro
(defmacro make-instruction 
  "make instructions"
  [name operation outputstack in1 in2]
  (list 'def name (list 'fn (vector 'state) 
                  (list 'make-push-instruction 'state operation 
                  (vector in1 in2)  outputstack))))


(def instruction-sets
  '(integer_+ +' :integer :integer :integer))

;map make-instruction across instruction-sets

(def integer_+
  "Adds the top two integers and leaves result on the integer stack.
  If integer stack has fewer than two elements, noops."
  (fn [state](make-push-instruction state +' [:integer :integer] :integer)))

 
(def integer_-
  "Subtracts the top two integers and leaves result on the integer stack.
  Note: the second integer on the stack should be subtracted from the top integer."
  (fn [state] (make-push-instruction state -' [:integer :integer] :integer)))

(defn integer_*
  "Multiplies the top two integers and leaves result on the integer stack."
  (fn [state] (make-push-instruction state *' [:integer :integer] :integer))))
 
(defn integer_%
  "This instruction implements 'protected division'.
  In other words, it acts like integer division most of the time, but if the
  denominator is 0, it returns the numerator, to avoid divide-by-zero errors."
  (fn [state] (make-push-instruction state (fn [x y] (if (= 0 y) x (/ x y))) [:integer :integer] :integer))))

(defn integer_>
  "Pushes true onto the boolean stack if the second
  item is greater than the top item, else false"
  (fn [state] (make-push-instruction state (fn [x y] (> y x)) [:integer :integer] :boolean))))

(defn integer_<
  "Pushes true onto the boolean stack if the second
  item is less than the top item, else false."
  (fn [state] (make-push-instruction state (fn [x y] (< y x)) [:integer :integer] :boolean))))
