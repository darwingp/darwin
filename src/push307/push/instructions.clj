(ns push307.push.instructions
  (:gen-class))

(defn in1
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack.
  Can't use make-push-instruction, since :input isn't a stack, but a map."
  [state]
  (cons (:in1 (:input state)) (:exec state))
  )

; (defn makeInstruction
;   [stack]
; )

(defn integer_+
  "Adds the top two integers and leaves result on the integer stack.
  If integer stack has fewer than two elements, noops."
  [state]
  (make-push-instruction state +' [:integer :integer] :integer))


(defn integer_-
  "Subtracts the top two integers and leaves result on the integer stack.
  Note: the second integer on the stack should be subtracted from the top integer."
  [state]
  (make-push-instruction state -' [:integer :integer] :integer))

(defn integer_*
  "Multiplies the top two integers and leaves result on the integer stack."
  [state]
  (make-push-instruction state *' [:integer :integer] :integer))
  )

(defn integer_%
  "This instruction implements 'protected division'.
  In other words, it acts like integer division most of the time, but if the
  denominator is 0, it returns the numerator, to avoid divide-by-zero errors."
  [state]
  (make-push-instruction state (fn [x y] (if (= 0 y) x (/ x y))) [:integer :integer] :integer))
  )

(defn integer_>
  "Pushes true onto the boolean stack if the second
  item is greater than the top item, else false"
  [state]
  (make-push-instruction state (fn [x y] (> y x)) [:integer :integer] :boolean))
  )

(defn integer_<
  "Pushes true onto the boolean stack if the second
  item is less than the top item, else false."
  [state]
  (make-push-instruction state (fn [x y] (< y x)) [:integer :integer] :boolean))
  )
