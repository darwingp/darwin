(ns push307.pushgp.crossover
  (:gen-class))

(defn uniform-crossover
  "Crosses over two programs or genomes (note: not individuals) using uniform crossover.
   Returns child program."
  [a b]
  (let [min-len (min (count a) (count b))
        ;get random value within length difference to prevent lower or upper length trend
        final-len (Math/round (* (rand) (- (max (count a) (count b)) min-len)))
        ap (take min-len a)
        bp (take min-len b)
        xs (if (= min-len (count a)) (drop min-len b) (drop min-len a))]
    (concat (map #(if (= (rand-int 2) 1) %1 %2) ap bp) (take final-len xs))))

(defn add-noise
  "returns gaussian noise for alternation crossover index modification"
  ; CITE: https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
  ; DESC: The Box-Muller method for generating uniformly distributed random numbers
  [alignment]   ;standard deviation (usually around 10)
  (let [u (rand) v (rand)]
    (Math/round
    (* alignment
     (* (Math/sqrt (* -2 (Math/log u)))
        (Math/cos (* 2 Math/PI v)))))))

(defn alternation-crossover
  "Crosses over two programs or genomes (note: not individuals) using alternation crossover
  takes alternation rate and alignment-deviation"
  ;alternation
  [prog-a prog-b alternation-rate alignment-deviation]
  (loop [index 0 child '() pa prog-a pb prog-b]
    (if (or
        (>= (Math/abs index) (count prog-a))
        (>= (Math/abs index) (count prog-b)))
      (reverse child)
      (do
        (if (< (rand) alternation-rate)
          (recur (+ index (add-noise alignment-deviation)) (cons (nth pa (Math/abs index)) child) pb pa)
          (recur (+ index 1) (cons (nth pa (Math/abs index)) child) pa pb))))))
