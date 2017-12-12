(ns darwin.push
  (:require [darwin.push.instructions :refer :all])
  (:require [darwin.push.utilities :refer :all])
  (:gen-class))

(defn interpret-one-step
  "Helper function for interpret-push-program.
  Takes a Push state and executes the next instruction on the exec stack,
  or if the next element is a literal, pushes it onto the correct stack.
  Returns the new Push state."
  [push-state]
  (let [v (peek-stack push-state :exec)
        popped (pop-stack push-state :exec)
        stack (stack-for v)]
    (cond
      (not (nil? stack)) (push-to-stack popped stack v) ;; see stack-for docstring
      (fn? v) (v popped) ;; v is a function
      (symbol? v) ((ns-resolve 'darwin.push.instructions v) popped) ;; v is a symbol pointing to a function
      (coll? v) (prepend-stack v :exec popped)
      :else (println (str "unexpected value: " v)))))

(defn interpret-push-program
  "Runs the given program starting with the stacks in start-state. Continues
  until the exec stack is empty. Returns the state of the stacks after the
  program finishes executing."
  [program start-state]
  (loop [st (prepend-stack
             program
             :exec
             start-state)] ;; push the program to the exec stack
    (if (empty-stack? st :exec)
      st
      (recur (interpret-one-step st)))))
