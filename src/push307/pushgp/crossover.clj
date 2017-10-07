(ns push307.pushgp.crossover
  (:gen-class))

(def get-gene (fn [x y]
                (if (= (rand-int 2) 1) x y)))

(defn uniform-crossover
   "Crosses over two programs (note: not individuals) using uniform crossover.
   Returns child program."
   [prog-a prog-b]
   (map get-gene prog-a prog-b)
   )

(defn add-noise
  "returns gaussian noise for alternation crossover index modification"
  ; CITE: https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
  ; DESC: The Box-Muller method for generating uniformly distributed random numbers
  [alignment]   ;standard deviation (usually around 10)
  (let [u (rand) v (rand)]
    (* alignment
    (Math/round
     (* (Math/sqrt (* -2 (Math/log u)))
        (Math/cos (* 2 Math/PI v))))
  )))


;NOTES:
;-------------------
; Testing alternation crossover 1000 times with prog sizes of 30,
;0.2 alternation rate, 4 alignment deviation yielded an average size of 48.
; Code:
; (/ (reduce + (repeatedly 10000 #(count (alternation-crossover (range 40) (range 40) 0.2 4)))) 10000)
; This dropped to 45 with 0.3, 3
; I will test it more to see how we can mitigate bloat...

(defn ensure-pos
  ""
  [x]
  (if (< x 0) 0 x))

(defn alternation-crossover
  "Crosses over two programs (note: not individuals) using alternation crossover
  takes alternation rate and alignment-deviation"
  ;alternation
  [prog-a prog-b alternation-rate alignment-deviation]

  (loop [index 0 child '() pa prog-a pb prog-b]
    (if (or
        (>= (ensure-pos index) (count prog-a))
        (>= (ensure-pos index) (count prog-b)))
      (reverse child)
      (do
        (if (< (rand) alternation-rate)
          (recur (+ index (add-noise alignment-deviation)) (cons (nth pa (Math/abs index)) child) pb pa)
           (recur (+ index 1) (cons (nth pa (Math/abs index)) child) pa pb)
    )))))
