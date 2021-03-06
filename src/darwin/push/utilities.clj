(ns darwin.push.utilities
  (:gen-class))

(defmacro definstr
  "Macro for defining Push instructions. Position 0 is the deepest arg,
   the last position is the top of the stack. Defines the instruction
   inside darwin.push.instructions & the current namespace."
  [name arg-stacks outputstack operation]
  (list 'let ['body (list 'fn '[state]
                  (list 'make-push-instruction 'state operation
                  arg-stacks outputstack))]
    (list 'intern ''darwin.push.instructions (list 'quote name) 'body)
    (list 'intern *ns* (list 'quote name) 'body)))

;;
;; NB: stacks are lists where the head of a stack is (first stack).
;;

(defn stack-for
  "Returns a keyword to the stack that x should reside on,
   or nil if no stack is appropriate."
  [x]
  (cond
    (integer? x) :integer
    (float? x) :float
    (string? x) :string
    (= true x) :boolean
    (= false x) :boolean
    :else nil))

(defn push-to-stack
  "Pushes item onto stack in state, returning the resulting state."
  [state stack item]
  (assoc state stack (cons item (get state stack '()))))

(defn pop-stack
  "Removes top item of stack, returning the resulting state."
  [state stack]
  (assoc state stack (rest (get state stack '()))))

(defn pop-n-stack
  "Removes an arbitrary number of elements from the top of the stack,
   returning the resulting state"
  [state stack n]
  (loop [nrem n
         prevstate state]
    (if (= 0 nrem)
      prevstate
      (recur (- nrem 1) (pop-stack prevstate stack)))))

(defn peek-stack
  "Returns top item on a stack. If stack is empty, returns :no-stack-item"
  [state stack]
  (let [s (get state stack '())]
    (if (empty? s)
      :no-stack-item
      (first s))))

(defn empty-stack?
  "Returns true if the stack is empty in state."
  [state stack]
  (empty? (get state stack '())))

;; WRITTEN BY Professor Helmuth
(defn get-args-from-stacks
  "Takes a state and a list of stacks to take args from. If there are enough args
  on each of the desired stacks, returns a map of the form {:state :args}, where
  :state is the new state with args popped, and :args is a list of args from
  the stacks. If there aren't enough args on the stacks, returns :not-enough-args."
  [state stacks]
  (loop [state state
         stacks (reverse stacks)
         args '()]
    (if (empty? stacks)
      {:state state :args args}
      (let [stack (first stacks)]
        (if (empty-stack? state stack)
          :not-enough-args
          (recur (pop-stack state stack)
                 (rest stacks)
                 (conj args (peek-stack state stack))))))))

(defn prepend-stack
  "Concats coll onto the front of stack in state."
  [coll stack state]
  (assoc state stack (concat coll (get state stack '()))))

(defn push-return-stack
  "Pushes a/many return value(s) to the stack(s).
   If the value is a vector, items are prepended in order.
   If the value is a map, return-stack is ignored, and for
   each k/v pair: push v onto stack k. Maps can contain vectors
   of values to prepend. Otherwise, the value is pushed onto return-stack."
  [state return-stack result]
  (cond
    (vector? result) (prepend-stack result return-stack state)
    (map? result) (reduce-kv push-return-stack state result)
    :else (push-to-stack state return-stack result)))

;; WRITTEN BY Professor Helmuth
(defn make-push-instruction
  "A utility function for making Push instructions. Takes a state, the function
  to apply to the args, the stacks to take the args from, and the stack to return
  the result to. Applies the function to the args (taken from the stacks) and pushes
  the return value onto return-stack in the resulting state.

  The first arguments to `function' are the deepest elements in their stack, the
  last arguments to `function' are closer to the top of their stack."
  [state function arg-stacks return-stack]
  (let [args-pop-result (get-args-from-stacks state arg-stacks)]
    (if (= args-pop-result :not-enough-args)
      state
      (let [result (apply function (:args args-pop-result))
            new-state (:state args-pop-result)]
        (push-return-stack new-state return-stack result)))))

(defn makemultipleinstr
  "Creates a Push instruction with an arity of n."
  [instack n outputstack operation]
  (fn [state]
    (make-push-instruction state operation (repeat n instack) outputstack)))

(defn mk-inputs
  "Takes a state and sets multiple inputs (in1, in2, ...) on it
   based on the values in the input list inputs."
  [inputs]
  (apply
    hash-map
    (flatten
      (map-indexed
        (fn [idx x] [(keyword (str "in" (inc idx))) x])
        inputs))))

(defn make-input-instruction
  "Returns an instruction to push input i to the exec stack."
  [i]
  #(push-return-stack % :exec ((keyword (str "in" i)) (:input %))))

(defn set-input
  "Sets an arbitrary input on a state."
  [state n v]
  (let [inpts (:input state)
        input-key (keyword (str "in" n))]
    (assoc state :input (assoc inpts input-key v))))
