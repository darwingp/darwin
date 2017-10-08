(ns push307.pushgp.generation
  (:gen-class))

;The following configure the random generation of programs
;----
;rand-prog-length is a low end value for a percent range used to randomly reduce
;the max-initial-program-size length: for n, the actual length will be n%-100% of input
(def rand-prog-length% 0.80) ;%low end of max-initial random range
(def literal% 0.40) ; Likelihood (percent) that a literal will be added to a random program.

(def gen-rand-length
  "generate a random % between rand-prog-length and 100%"
  (fn [] (+ (rand (- 1 rand-prog-length%)) rand-prog-length% )))

(def get-random-push
  "returns a random instruction or random literal depending in percent likelihood of literal"
  (fn [inst lit]
    (if (< (rand) literal%)     ;random decision based on percent literals
      (nth lit (rand-int (count lit)))
      (nth inst (rand-int (count inst))))))

(defn generate-random-program
  "Creates and returns a new program (note: not individual).
   Takes a list of instructions and a maximum initial program size."
  [instructions literals max-initial-program-size]
  ;create random program length within range n%-100% max-initial-program-size
  (let [progsize (Math/round (* max-initial-program-size (gen-rand-length)))]
    (repeatedly progsize #(get-random-push instructions literals))))
