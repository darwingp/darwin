(ns push307.push
  (:require [push307.push.instructions :refer :all])
  (:require [push307.push.utilities :refer :all])
  (:gen-class))


  ;;;;;;;;;;
  ;; Instructions must all be either functions that take one Push
  ;; state and return another or constant literals.
  (def instructions
    (list
     'in1
     'integer_+
     'integer_-
     'integer_*
     'integer_%
     0
     1
     ))
     ;
     ; ; An example Push state
     ; (def example-push-state
     ;   {:exec '(integer_+ integer_-)
     ;    :integer '(1 2 3 4 5 6 7)
     ;    :string '("abc" "def")
     ;    :input {:in1 4 :in2 6}})
     ;
     ; ; An example Push program
     ; (def example-push-program
     ;   '(3 5 integer_* "hello" 4 "world" integer_-))
     ;

  ;;;;;;;;;;
  ;; Interpreter

  (defn interpret-one-step
    "Helper function for interpret-push-program.
    Takes a Push state and executes the next instruction on the exec stack,
    or if the next element is a literal, pushes it onto the correct stack.
    Returns the new Push state."
    [push-state]
    :STUB
    )

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
