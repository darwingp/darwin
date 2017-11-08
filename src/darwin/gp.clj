(ns darwin.gp
  (:require [darwin.push.instructions :refer [ins]])
  (:require [darwin.push.utilities :refer :all])
  (:require [darwin.gp.utilities :refer :all])
  (:require [darwin.gp.mutation :refer :all])
  (:require [darwin.push.generation :refer :all])
  (:require [darwin.plush.generation :refer :all])
  (:require [darwin.graphics.plotter :refer :all])
  (:gen-class))

(defn to-integer
  "Takes literally anything and converts it into an integer"
  [v]
  (cond (keyword? v) (to-integer (name v))
        (string? v) (Integer. v)
        :else v))

(defn percentaged-or-not
  "Takes either a collection (list of 2-tuples or a map) or a sigle value.
   If a collection, the first element of each tuple is a percentage, and the second
   value is the value associated with the percentage. Returns a random value
   respecting the percentages. Otherwise, returns the argument."
   [col default]
   (if (not (coll? col))
     col
     (loop [v-remaining (rand-int 100)
            percs col]
       (if (empty? percs)
         default
         (let [pair (first percs)
               perc (to-integer (nth pair 0))
               v (nth pair 1)]
           (if (< v-remaining perc) ;; FIXME: is this correct?
             v
             (recur (- v-remaining perc) (rest percs))))))))

(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. The selection operator, crossover operator, operator
  probabilities and event percentages are determined from provided configuration."
  [genomic instructions literals percent-literals population config]
  (let [getter (if genomic :genome :program)
        selection-f (percentaged-or-not (:selection config) nil)
        select #(getter (selection-f population)) ;; Returns a program/genome
        crossover (:crossover config) ;; Takes two programs/genomes and returns one program/genome

        ;; {Genome/Program}-appropriate mutation operators
        add-op (if genomic uniform-addition-genome uniform-addition)
        mut-op (if genomic uniform-mutation-genome uniform-mutation)

        ;; :mutation, :deletion, :addition, or :crossover
        op (percentaged-or-not (:percentages config) :mutation)]
    {getter
     (cond
        (= op :crossover) (crossover
                            (select)
                            (select))
        (= op :deletion) (uniform-deletion
                          (:deletion-percent config)
                          (select))
        (= op :addition) (add-op
                          instructions
                          literals
                          percent-literals
                          (:addition-percent config)
                          (select))
        :else            (mut-op
                           instructions
                           literals
                           percent-literals
                           (:mutation-percent config)
                           (select)))
     }))

(defn best-n-errors
  "Given a population and some number n, returns the
   lowest n errors in population"
  [pop n]
  (take n (sort (map :total-error pop))))

(defn print-many-ln
  "Prints args to stdout in a coder-friendly way."
  [& args]
  (println (apply str (map print-str args))))

(defn report
  "Reports information on a generation."
  [population generation-num behavioral-diversity-func]
    (let [best (best-overall-fitness population)
          ;; TODO: not report -1 for behavioral diversity if it's
          ;;       not implemented.
          behavioral-diversity (if (nil? behavioral-diversity-func)
                                 -1
                                 (behavioral-diversity-func population))
          current-state {
                         :points-fit (:total-error best)
                         :points-behavior behavioral-diversity
                         :average-fitness (:total-error best)
                         :best-size (count (:program best))
                         :generation generation-num
                         }]

     ;; plot data points
     (add-pt current-state :points-fit line-color-1)
     (add-pt current-state :points-behavior line-color-2)
     (add-pt current-state :average-fitness line-color-3)
     (add-pt current-state :best-size line-color-4)

     ;; print stats to the console
     (print-many-ln "------------------------------------")
     (print-many-ln "        Report for Generation " generation-num)
     (print-many-ln "------------------------------------")
     (print-many-ln "Best individual: " (:program best))
     (print-many-ln " -> errors: " (:errors best))
     (print-many-ln " -> total error: " (:total-error best))
     (print-many-ln " -> size: " (count (:program best)))
     (print-many-ln "Best 20 errors: " (best-n-errors population 20))
     (print-many-ln "Behavioral Diversity: " behavioral-diversity)))

(defn population-has-solution
  "Returns true if population has a program with zero error.
   False otherwise."
  [population]
  (not (nil? (find-list zero? (map :total-error population)))))

(defn evaluate-individual
  [inputses testcases xform individual]
  (let [ind (prepare-individual individual)
        ran (assoc
              ind
              :exit-states
              (map
                #(run-individual % ind)
                inputses))
        xformed (if (nil? xform) ran (xform ran))]
    (test-individual testcases xformed)))

(defn make-generations
  "Returns a lazily-evaluated, Aristotelian infinite list
   representing all countable generations."
  [genomic
   population-size
   instrs
   literals
   percent-literals
   max-initial-program-size
   min-initial-program-size
   inputses
   testcases
   evolution-config]
  (let [wrap #(evaluate-individual inputses testcases (:individual-transform evolution-config) %)
        generate (if genomic generate-random-genome generate-random-program)]
    (iterate
     (fn [population]
       (prepeatedly
         population-size
         #(wrap
           (select-and-vary
             genomic
             instrs
             literals
             percent-literals
             population
             evolution-config))))
     (prepeatedly
       population-size
        #(wrap
          (generate
            instrs
            literals
            percent-literals
            max-initial-program-size
            min-initial-program-size))))))

(defn run-gp
  "Initializes a population, and then repeatedly
  generates and evaluates new populations. Stops and returns :SUCCESS
  if it finds an individual with 0 error, or if it exceeds the maximum
  generations it returns nil. Print reports each generation.
  --
  Takes one argument: a map containing the core parameters to
  push-gp. The map's keys should include:
   - population-size (an integer)
   - max-generations (an integer)
   - testcases (a list of test cases)
   - inputses (a list of inputses (see README terminolog: inputs))
   - program-arity (the number of inputs your evolved program takes)
   - instructions (a list of instructions)
   - literals (a list of literals)
   - number-inputs (the number of inputs the program will take)
   - max-initial-program-size (max size of randomly generated programs)
   - min-initial-program-size (minimum size of randomly generated programs)
   - initial-percent-literals (how much of randomly generated programs/genomes should be literals, a float from 0.0 to 1.0)
   - evolution-config (a map)
     - selection (fn that takes a population and returns an individual)
     - crossover (fn that takes two programs/genomes and returns a program/genome)
     - percentages (list of tuples w/ an integer % in position 0 and a keyword in position 1)
     - deletion-percent (an integer from 0 to 100)
     - addition-percent (an integer from 0 to 100)
     - mutation-percent (an integer from 0 to 100)
     - individual-transform (a function that takes an individual and returns an individual. Applied before select-and-vary)
   - behavioral-diversity (a function to calculate behavioral diversity given a population)"
  [{:keys [population-size max-generations testcases error-function instructions inputses program-arity literals max-initial-program-size min-initial-program-size initial-percent-literals genomic evolution-config behavioral-diversity individual-transform]}]
  (start-plotter)
  (let [all-inputs (take program-arity ins) ; generate in1, in2, in3, ...
        gens (take
               max-generations
               (make-generations
                 genomic
                 population-size
                 (concat all-inputs instructions)
                 literals ;; THINK ON: should inputs be considered literals
                          ;; or instructions?
                 initial-percent-literals
                 max-initial-program-size
                 min-initial-program-size
                 inputses
                 testcases
                 evolution-config))
        solution (find-list
                   population-has-solution
                   (map-indexed
                     #(do
                       (report %2 (inc %1) behavioral-diversity)
                       %2)
                     gens))]
    (if (nil? solution) nil :SUCCESS)))
