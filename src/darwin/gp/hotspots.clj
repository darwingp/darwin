(ns darwin.gp.hotspots
  (:gen-class))

;; Genetic Hotspots (based partially on ALPS)

;; (defn hot?
;;   [gene threshold]
;;   (let [thold (max threshold 1)
;;         heat (get gene :heat 0)
;;         perc-difference (if (< heat thold)
;;                           (float (/ heat thold))
;;                           (float (/ thold heat)))]
;;     (< perc-difference 0.25)))

(defn hot?
  [gene threshold]
  (< (get gene :heat 0) (max threshold 1)))

(defn avg
  [nums]
  (cond
    (empty? nums) 0
    (= 1 (count nums)) (first nums)
    :else (/ (reduce +' nums) (count nums))))

(defn avg-heat
  "Given any number of genes, return the average :heat."
  [& genes]
  (avg (map #(get % :heat 0) genes)))

(defn fmap-heat
  "Given a gene, applies a function f to its heat,
   returning the gene with :heat set to whatever f returned."
  [f gene]
  (assoc gene :heat (f (get gene :heat 0))))

(defn inc-heat
  [gene]
  (fmap-heat #(min (inc %) 50) gene))

(defn insert-hot
  "Sequentially replaces hot genes in `genome' with genes
   from `new-hotgenes'. Uses heat-threshold to determine
   which genes to replace"
  [genome new-hotgenes heat-threshold]
  (loop [g genome
         result []
         new-genes new-hotgenes]
    (cond
      (empty? g) (seq result)
      (empty? new-genes) (concat result g)
      (hot? (first g) heat-threshold) (recur
                                        (rest g)
                                        (conj result (first new-genes))
                                        (rest new-genes))
      :else (recur
              (rest g)
              (conj result (first g))
              new-genes))))

(defn wrap
  "Wraps another genetic operator (of any arity) with genetic hotspots. f is only
   applied over 'hot' genes."
  [f]
  (fn [& args]
    (let [avg-gene-heat (avg (map #(get % :heat 0) (apply concat args)));; Damn obvious
          hotargs (map (fn [x] (filter #(hot? % avg-gene-heat) x)) args)]
      (insert-hot
        (first args)
        (apply f hotargs)
        avg-gene-heat))))

