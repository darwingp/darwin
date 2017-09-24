(ns push307.push.utilities
  (:gen-class))

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
  (assoc state stack (cons item (stack state))))

(defn push-many-to-stack
  "Pushes multiple items to the stack at once. Vectors are copied in order
   to on top of the stack and lists are copied in reverse to on top of the stack."
  [state stack items]
  (let [newstack (if (vector? items)
                     (concat items (stack state)) ;; emulate looping push: (reduce #(push-to-stack % stack %1) state items)
                     (concat (reverse items) (stack state)))] ;; concat vectors onto the top of the stack in order
    (assoc state stack newstack)))

(defn pop-stack
  "Removes top item of stack, returning the resulting state."
  [state stack]
  (assoc state stack (rest (stack state))))

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
  (if (empty? (stack state))
    :no-stack-item
    (first (stack state))))

(defn empty-stack?
  "Returns true if the stack is empty in state."
  [state stack]
  (empty? (stack state)))

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

(defn push-return-stack
  "Pushes a/many return value(s) to the stack(s).
   If the value is a list/vector, items are pushed using
   push-many-to-stack. If the value is a map, return-stack
   is ignored, and for each k/v pair: push v onto stack k.
   Maps can contain lists of values to push.

   Otherwise, the value is pushed onto the stack in accordance with
   Professor Helmuth's original make-push-instruction implementation."
  [state return-stack result]
  (cond
    (or (list? result) (vector? result)) (push-many-to-stack state return-stack result)
    (map? result) (reduce-kv
                    (fn [m k v] 
                      (if (or (list? v) (vector? v))
                        (push-many-to-stack m k v)
                        (push-to-stack m k v)))
                    state
                    result) ;;; FIXME: this pushes individual items backwards!!!
    :else (push-to-stack state return-stack result)))

;; WRITTEN BY Professor Helmuth
(defn make-push-instruction
  "A utility function for making Push instructions. Takes a state, the function
  to apply to the args, the stacks to take the args from, and the stack to return
  the result to. Applies the function to the args (taken from the stacks) and pushes
  the return value onto return-stack in the resulting state."
  [state function arg-stacks return-stack]
  (let [args-pop-result (get-args-from-stacks state arg-stacks)]
    (if (= args-pop-result :not-enough-args)
      state
      (let [result (apply function (:args args-pop-result))
            new-state (:state args-pop-result)]
        (push-return-stack new-state return-stack result)))))

(defmacro definstr
  "Macro for defining Push instructions. Position 0 is the deepest arg,
   the last position is the top of the stack."
  [name arg-stacks outputstack operation]
  (list 'def name (list 'fn '[state]
                  (list 'make-push-instruction 'state operation 
                  arg-stacks outputstack))))
