(ns push307.pushgp.generation
  (:gen-class))

(defn generate-random-program
  "Creates and returns a new program (note: not individual).
   Takes a list of instructions and a maximum initial program size."
  [instructions max-initial-program-size]
  ;TODO: generators vs. loop
  ;(loop [prog '() count max-initial-program-size ])
  '(2 1 integer_+)) ;; TODO: actually generate a program
