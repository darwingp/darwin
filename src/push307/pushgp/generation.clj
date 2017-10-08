(ns push307.pushgp.generation
  (:gen-class))

;The following configure the random generation of programs
;----
;rand-prog-length is a low end value for a percent range used to randomly reduce
;the max-initial-program-size length: for n, the actual length will be n%-100% of input
(def rand-prog-length% 0.80) ;%low end of max-initial random range
(def literal% 0.2) ; Likelihood (percent) that a literal will be added to a random program.

(defn get-random-push
  "returns a random instruction or random literal depending in percent likelihood of literal"
   [inst lit]
   (if (< (rand) literal%)     ;random decision based on percent literals
     (nth lit (rand-int (count lit)))
     (nth inst (rand-int (count inst)))))

(def min-prog-size 10)

(defn generate-random-program
  "Creates and returns a new program (note: not individual).
   Takes a list of instructions and a maximum initial program size."
  [instructions literals max-initial-program-size]
  (repeatedly
    (+ (Math/round (* (- max-initial-program-size min-prog-size) (rand))) min-prog-size)
    #(get-random-push instructions literals)))
