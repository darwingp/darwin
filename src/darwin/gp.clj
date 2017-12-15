(ns darwin.gp
  (:require [darwin.push.literals :refer [ins]])
  (:require [darwin.utilities :refer :all])
  (:require [darwin.push.utilities :refer :all])
  (:require [darwin.gp.utilities :refer :all])
  (:require [darwin.gp.mutation :refer :all])
  (:require [darwin.gp.selection :refer :all])
  (:require [darwin.gp.crossover :refer :all])
  (:require [darwin.gp.hotspots :refer :all])
  (:require [darwin.graphics.plotter :refer :all])
  (:gen-class))

(defn print-many-ln
  "Prints args to stdout in a coder-friendly way."
  [& args]
  (println (apply str (map print-str args))))

(defn to-integer
  "Takes literally anything and converts it into an integer"
  [v]
  (cond (keyword? v) (to-integer (name v))
        (symbol? v) (to-integer (str v))
        (string? v) (bigint v)
        :else v))

(defn percentaged-or-not
  "Takes either a collection (list of 2-tuples or a map) or a sigle value.
   If a collection, the first element of each tuple is a percentage, and the second
   value is the value associated with the percentage. Returns a random value
   respecting the percentages. Otherwise, returns the argument."
   [col default]
   (cond
     (nil? col) default
     (not (coll? col)) col
     :else (loop [perc-remaining (rand-int 100)
                  percs col]
             (if (empty? percs)
               default
               (let [pair (first percs)
                     perc (to-integer (first pair))
                     v (second pair)]
                 (if (< perc-remaining perc) ;; FIXME: is this correct?
                   v
                   (recur (- perc-remaining perc) (rest percs))))))))

(defn percentage-map-choice
  [percentable map default-key]
  ((percentaged-or-not percentable default-key) map))

(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. The selection operator, crossover operator, operator
  probabilities and event percentages are determined from provided configuration."
  [genomic select crossover deletion addition mutation op new-element config population]
    {(if genomic :genome :program)
     (cond
        (= op :crossover) (crossover
                            (select)
                            (select))
        (= op :deletion) (deletion
                           (:deletion-percent config)
                           (select))
        (= op :addition) (addition
                           new-element
                           (:addition-percent config)
                           (select))
        (= op :copy)     ((if genomic :genome :program) (rand-nth population))
        :else            (mutation
                           new-element
                           (:mutation-percent config)
                           (select)))})

(defn report
  "Reports information on a generation."
  [population generation-num behavioral-diversity-func]
    (let [best (best-overall-fitness population)
          best-twenty (take 20 (sort (map :total-error population)))
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

     (locking *out*
       ;; print stats to the console
       (print-many-ln "------------------------------------")
       (print-many-ln "        Report for Generation " generation-num)
       (print-many-ln "------------------------------------")
       (print-many-ln "Best individual: " (:program best))
       (print-many-ln " -> errors: " (:errors best))
       (print-many-ln " -> total error: " (:total-error best))
       (print-many-ln " -> size: " (count (:program best)))
       ;(print-many-ln " -> exit state: " (:exit-states best))
       (print-many-ln "Best 20 errors: " best-twenty)
       (print-many-ln "Behavioral Diversity: " behavioral-diversity))))

(defn population-has-solution
  "Returns true if population has a program with zero error.
   False otherwise."
  [population]
  (not (nil? (find-list zero? (map :total-error population)))))

(defn evaluate-individual
  [inputses testcases xform individual] ;TODO: novelty param
  (let [ind (prepare-individual individual)
        ran (assoc
              ind
              :exit-states
              (map
                #(run-individual % ind)
                inputses))
        xformed (if (nil? xform) ran (xform ran))]
    (test-individual testcases xformed)))

(defn make-fn-new-element
  [instructions literals inputs config]
  (let [event-map { :instruction instructions :literal literals :input inputs }]
    #(rand-nth (percentage-map-choice config event-map :instruction))))

(defn generate
  "Generate a new individual."
  [genomic new-element config]; instructions literals inputs config]
  (let [max-size (get config :maximum-size 100)
        min-size (get config :minimum-size 20)
        k (if genomic :genome :program)]
    {k (repeatedly
         (+ (Math/round (* (- max-size min-size) (rand))) min-size)
         new-element)}))

(defn get-heat
  [v heatinfo]
  (cond
    (map? heatinfo) (get heatinfo v 0)
    (integer? heatinfo) heatinfo
    :else 0))

(defn prepare-valuelist
  "IN: genomic flag
       heatmap
       aritymap
       list of values to be used in push
   OUT: prepared for use with GP"
  [genomic heat arity values]
  (if genomic (map #(gene-wrap (get arity % 0) (get-heat % heat) %) values) values))

(defn make-generations
  "Returns a lazily-evaluated, Aristotelian infinite list
   representing all countable generations."
  [genomic
   population-size
   instr-arities
   instrs
   literals
   inputs
   instr-heat
   literal-heat
   input-heat
   generation-config
   inputses
   testcases
   evolution-config]
  (let [xform (:individual-transform evolution-config)
        wrap #(evaluate-individual inputses testcases xform %)
        instrs-universal (prepare-valuelist genomic instr-heat instr-arities instrs)
        lits-universal (prepare-valuelist genomic literal-heat {} literals)
        inputs-universal (prepare-valuelist genomic input-heat {} inputs)
        should-age-heat (get evolution-config :decrease-heat-by-age false)
        selection-f (percentaged-or-not
                      (:selection evolution-config)
                      #(tournament-selection % (quot (count %) 20)))
        crossover (percentaged-or-not (:crossover evolution-config) uniform-crossover)
        deletion (percentaged-or-not (:deletion evolution-config) uniform-deletion)
        addition (percentaged-or-not (:addition evolution-config) uniform-addition)
        mutation (percentaged-or-not (:mutation evolution-config) uniform-mutation)
        op (percentaged-or-not (:percentages evolution-config) :mutation)
        new-element (make-fn-new-element
                      instrs-universal
                      lits-universal
                      inputs-universal
                      (:new-element evolution-config))
        new-element-generation (make-fn-new-element
                                 instrs-universal
                                 lits-universal
                                 inputs-universal
                                 (:composition generation-config))]
    (iterate
     (fn [population]
       (let [select #(if genomic
                       (if should-age-heat
                         (map inc-heat (:genome (selection-f population)))
                         (:genome (selection-f population)))
                       (:program (selection-f population)))]
         (prepeatedly
           population-size
           #(wrap
             (select-and-vary
               genomic
               select
               crossover
               deletion
               addition
               mutation
               op
               new-element
               evolution-config
               population)))))
     (prepeatedly
       population-size
        #(wrap
          (generate
            genomic
            new-element-generation
            generation-config))))))

(defn run-gp
  "Initializes a population, and then performs the genetic programming
   algorithm upon the population. See README.md (Configuration section)
   Stops and returns :SUCCESS if it finds an individual with 0 error,
   or if it exceeds the maximum generations it returns nil. Print
   reports each generation."
  [{:keys [population-size
           max-generations
           testcases
           inputses
           program-arity
           instructions
           literals
           instruction-heat
           literal-heat
           input-heat
           generation
           genomic
           evolution-config
           behavioral-diversity
           individual-transform
           instruction-arities]}]
  (start-plotter)
  (let [gens (take
               max-generations
               (make-generations
                 genomic
                 population-size
                 instruction-arities
                 instructions
                 literals
                 (take program-arity ins)
                 instruction-heat
                 literal-heat
                 input-heat
                 generation
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
    ((get evolution-config :end-action (fn [_] ))
      (filter #(zero? (:total-error %)) solution))
    (if (nil? solution) nil :SUCCESS)))
