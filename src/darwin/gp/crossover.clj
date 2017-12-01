(ns darwin.gp.crossover
  (:require [darwin.gp.utilities :refer :all])
  (:gen-class))

(defn truncate-lists
  "Returns a vector: [new-a new-b tail] where new-a and new-b are
   the the same length and tail is the remaining portion of either a or b."
  [a b]
  (let [len (min (count a) (count b))]
    [(take len a)
     (take len b)
     (drop len (if (> (count a) (count b)) a b))]))

;; Uniform Crossover

(defn uniform-crossover
  "Has an equal probability of selecting a gene/instruction/literal from either
   parent program when crossing them over. Takes two genomes or programs and
   return a genome or program."
  [a b]
  (let [[aa bb tail] (truncate-lists a b)]
    (concat (map #(if (= (rand-int 2) 1) %1 %2) aa bb) tail)))

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

;; TODO: make the heatmap mean a crossover happens,
;;       equal probability of picking from either a or b at that point.
;;       Maybe even allow for the use of another operator
(defn age-hotness-crossover
  "Performs crossover based on the age of genes in a genome."
  [a b]
  (let [[ta tb tail] (truncate-lists a b)
        avg-gene-age (apply avg-age (concat a b)) ;; Damn obvious
        heatmap (map #(< (avg-age %1 %2) avg-gene-age) ta tb) ;; percentages (0-100)
        crossed-over (map #(if %3 %1 %2) ta tb heatmap)]
      (concat crossed-over (map #(> (get % :age 0) avg-gene-age) tail))))

;; This is an idea that could be worked on farther
;; Performing crossover on hot genes only

(defn hot?
  [gene age-threshold]
  (let [thold (max age-threshold 1)
        age (get gene :age 0)
        perc-difference (if (< age thold)
                          (float (/ age thold))
                          (float (/ thold age)))]
    (< perc-difference 0.25)))
;  (< (get gene :age 0) age-threshold))

(defn insert-hot
  [genome new-hotgenes age-threshold]
  (loop [g genome
         result []
         new-genes new-hotgenes]
    (cond
      (empty? g) (seq result)
      (hot? (first g) age-threshold) (recur
                                       (rest g)
                                       (conj result (first new-genes))
                                       (rest new-genes))
      :else (recur
              (rest g)
              (conj result (first g))
              new-genes))))

(defn age-hotspot-wrap
  "Wraps another crossover operator with age-hotness. This means that
   whatever crossover operator f is, it only crosses over hot genes."
  [f]
  (fn [a b]
    (let [avg-gene-age (apply avg-age (concat a b)) ;; Damn obvious
          hota (filter #(hot? % avg-gene-age) a)
          hotb (filter #(hot? % avg-gene-age) b)]
      (insert-hot
        (if (true-percent? 50) a b)
        (f hota hotb)
        avg-gene-age))))
