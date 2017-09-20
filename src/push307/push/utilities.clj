(ns push307.push.utilities
  (:gen-class))

  ;;;;;;;;;;
  ;; Utilities

  (def empty-push-state
    {:exec '()
     :integer '()
     :string '()
     :input {}})

  ;;
  ;; NB: stacks are lists where the head of a stack is (first stack).
  ;;

  (defn push-to-stack
    "Pushes item onto stack in state, returning the resulting state."
    [state stack item]
    (assoc state stack (cons item (stack state))))

  ;; NB: this is an optimized implementation. The semantics are:
  ;;     (reduce #(push-to-stack % stack %1) state items)
  (defn push-many-to-stack
    "Pushes each item in items in order of appearance to stack in state, returning the resulting state."
    [state stack items]
    (assoc state stack (concat (reverse items) (stack state))))

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
    (first (stack state)))

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
           stacks stacks
           args '()]
      (if (empty? stacks)
        {:state state :args (reverse args)}
        (let [stack (first stacks)]
          (if (empty-stack? state stack)
            :not-enough-args
            (recur (pop-stack state stack)
                   (rest stacks)
                   (conj args (peek-stack state stack))))))))

  (defn make-push-instruction
    "A utility function for making Push instructions. Takes a state, the function
    to apply to the args, the stacks to take the args from, and the stack to return
    the result to. Applies the function to the args (taken from the stacks) and pushes
    the return value onto return-stack in the resulting state."
    [state function arg-stacks return-stack]
    (let [args-pop-result (get-args-from-stacks state arg-stacks)]
      (if (= args-pop-result :not-enough-args)
        state
        (let [result (apply function (reverse (:args args-pop-result)))
              new-state (:state args-pop-result)]
          (push-to-stack new-state return-stack result)))))
