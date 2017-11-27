(ns darwin.gp.utilities
  (:require [darwin.push.utilities :refer :all])
  (:require [darwin.push :refer :all])
  (:require [darwin.plush.translate :refer :all])
  (:gen-class))

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
  (let [start-state (assoc empty-push-state :input (mk-inputs inputs))]
    (interpret-push-program (:program individual) start-state)))

(defn test-individual
  "Performs each test in tests on each of the individual's exit states."
  [tests individual testattribute]
  (let [errors (flatten (map #(map % (:exit-states individual)) tests))]
    (merge individual (if testattribute
                      {:errors (map :error errors)
                       testattribute (map testattribute errors)
                       :total-error (reduce +' (map :error errors))}
                      {:errors errors
                       :total-error (reduce +' errors)}))))

(defn gene-wrap
  "Creates a gene given a value the gene represents."
  [arity v]
  { :value v :arity arity :close (if (true-percent? 5) (rand-int 4) 0) })

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
  (try (reduce
    #(if (< (:total-error %1) (:total-error %2))
      %1 %2)
    population) (catch Exception e (str "error "))))

(defn prepeatedly
  "Like repeatedly, but parallel."
  [n fn]
  (apply pcalls (repeat n fn)))
