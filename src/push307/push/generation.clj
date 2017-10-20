(ns push307.push.generation
  (:gen-class))

(defn binary-rand-nth
  "Returns a random member of collection 'a` 'bias-percent` of the
  time or a random member of collection 'b` the rest of the time.
  'bias-percent` is a float from 0.0 to 1.0"
  [bias-percent a b]
  (if (< (rand) bias-percent)
    (rand-nth a)
    (rand-nth b)))

(defn generate-random-program
  "Creates and returns a new program (note: not individual).
   Takes a list of instructions and a maximum initial program size."
  [instructions literals percent-literals max-size min-size]
  (repeatedly
    (+ (Math/round (* (- max-size min-size) (rand))) min-size)
    #(binary-rand-nth percent-literals literals instructions)))
