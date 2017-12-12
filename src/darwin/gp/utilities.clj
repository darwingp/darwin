(ns darwin.gp.utilities
  (:require [darwin.push.utilities :refer :all])
  (:require [darwin.push :refer :all])
  (:require [darwin.plush.translate :refer :all])
  (:gen-class))

(defn abs
  [n]
  (if (< n 0) (* -1 n) n))

(defn find-list
  "Finds the first element in l for which p is true"
  [p l]
  (first (drop-while #(not (p %)) l)))

(defn binary-rand-nth
  "Returns a random member of collection 'a` 'bias-percent` of the
  time or a random member of collection 'b` the rest of the time.
  'bias-percent` is a float from 0.0 to 1.0"
  [bias-percent a b]
  (if (< (rand) bias-percent)
    (rand-nth a)
    (rand-nth b)))

(defn true-percent?
  "Returns true n percent of the time."
  [n]
  (<= (inc (rand-int 100)) n))

; The default individual
(def default-individual { :errors '()
                          :total-error 0
                          :program '()
                          :exit-states '()})

(defn prepare-individual
  "Prepares an individual for running/testing. Takes an individual
   and returns a copy of it that's ready for use. This involves
   checking for the presence of a :genome and setting :program accordingly."
  [ind]
  (if (not (nil? (:genome ind)))
    (merge ind default-individual { :program (translate-plush-genome-to-push-program (:genome ind)) })
    (merge ind default-individual)))

(defn run-individual
  "Runs an individual with a list of inputs, storing the final Push
   state from the run in the individual's :exit-states list."
  [inputs individual]
  (interpret-push-program (:program individual) { :input (mk-inputs inputs) }))

(defn set-total-error
  [ind]
  (assoc ind :total-error (reduce +' (:errors ind))))

(defn flatten-maplist
  "Takes a list of maps, and returns a map where each key appears in maplist
   and the value is a list of all values for that key in maplist."
  [maplist]
  (loop [ms maplist
         res {}]
    (if
      (empty? ms)
      res
      (recur
       (rest ms)
       (reduce-kv
         #(assoc %1 %2
            (concat
              (get %1 %2 '())
              (list %3)))
         res
         (first ms))))))

(defn test-individual
  "Performs each test in tests on each of the individual's exit states."
  [tests individual]
  (let [errors (flatten (map #(map % (:exit-states individual)) tests))
        is-fitnesses (integer? (first errors))]
    (set-total-error
      (if is-fitnesses
        (assoc individual :errors errors)
        (let [flattened (flatten-maplist errors)]
          (merge
            (assoc individual :errors (get flattened :error '()))
            (dissoc flattened :error)))))))

(defn gene-wrap
  "Creates a gene given a value the gene represents."
  [arity heat v]
  { :value v :arity arity :close (if (true-percent? 5) (rand-int 4) 0) :heat heat})

;; any time a test is mentioned, it's the idx in the individual.

(defn error-on-test
  "Returns the error an individual got on a test"
  [individual test-idx]
  (nth (:errors individual) test-idx))

(defn best-fitness-in
  "Returns the most fit individual in the population given a test"
  [population test-idx]
  (reduce
    #(if (< (error-on-test %1 test-idx) (error-on-test %2 test-idx)) %1 %2)
    population))

(defn best-overall-fitness
  "Returns the member of the population with the the lowest total error."
  [population]
  (reduce
    #(if (< (:total-error %1) (:total-error %2)) %1 %2)
    population))

(defn prepeatedly
  "Like repeatedly, but parallel."
  [n fn]
  (apply pcalls (repeat n fn)))
