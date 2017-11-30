(ns darwin.gp.crossover
  (:require [darwin.gp.utilities :refer :all])
  (:gen-class))

;; Uniform Crossover

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

;; Alternation Crossover

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
          (recur (+' index (add-noise alignment-deviation)) (cons (nth pa (Math/abs index)) child) pb pa)
          (recur (+' index 1) (cons (nth pa (Math/abs index)) child) pa pb))))))

;; Gene-Level ALPS

(defn avg
  [nums]
  (/ (reduce +' nums) (count nums)))

(defn avg-age
  "Given any number of genes, return the average age"
  [& genes]
  (avg (map #(get % :age 0) genes)))

(defn inc-age
  [gene]
  (assoc gene :age (min 50 (inc (get gene :age 0)))))

(defn truncate-lists
  "Returns a vector: [new-a new-b tail] where new-a and new-b are
   the the same length and tail is the remaining portion of either a or b."
  [a b]
  (let [len (min (count a) (count b))]
    [(take len a)
     (take len b)
     (drop len (if (> (count a) (count b)) a b))]))

(defn age-hotness-crossover
  "Performs crossover based on the age of genes in a genome."
  [a b]
  (let [[ta tb tail] (truncate-lists a b)
        avg-gene-age (apply avg-age (concat a b)) ;; Damn obvious
        heatmap (map #(> (avg-age %1 %2) avg-gene-age) ta tb) ;; percentages (0-100)
        crossed-over (map #(if %3 %1 %2) ta tb heatmap)]
      (concat crossed-over (map #(> % avg-gene-age) tail))))
