(ns push307.push
  (:require [push307.push.instructions :refer :all])
  (:require [push307.push.utilities :refer :all])
  (:gen-class))

(def empty-push-state
  {:exec '()   
   :integer '()
   :string '()
   :input {}})

(defn interpret-one-step
  "Helper function for interpret-push-program.
  Takes a Push state and executes the next instruction on the exec stack,
  or if the next element is a literal, pushes it onto the correct stack.
  Returns the new Push state."
  [push-state]
  (if (empty-stack? push-state :exec)
    push-state
    (let [v (peek-stack push-state :exec)
          popped (pop-stack push-state :exec)
          stack (stack-for v)]
      (cond
        (not (nil? stack)) (push-to-stack popped stack v) ;; see stack-for docstring
        (fn? v) (v popped) ;; v is a function
        (symbol? v) ((ns-resolve 'push307.push.instructions v) popped) ;; v is a symbol pointing to a function
        :else (print "unexpected value")))))

(defn interpret-push-program
  "Runs the given program starting with the stacks in start-state. Continues
  until the exec stack is empty. Returns the state of the stacks after the
  program finishes executing."
  [program start-state]
  ;; NB: This code will reverse program twice. Is that bad?
  (loop [st (push-many-to-stack
              start-state
              :exec
              (reverse program))] ;; push the program to the exec stack
    (if (empty-stack? st :exec)
      st
      (recur (interpret-one-step st)))))
