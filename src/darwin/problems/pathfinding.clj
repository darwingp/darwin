(ns darwin.problems.pathfinding
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.mutation :as mutation])
  (:require [darwin.gp.hotspots :as hotspots])
  (:require [darwin.gp.utilities :as utils])
  (:require [darwin.utilities :refer :all])
  (:require [darwin.problems.pathfinding.machine :as machine])
  (:require [darwin.problems.pathfinding.instructions :refer :all])
  (:gen-class))

(def instructions
  '(new_angle
    ;set_speed
    ;new_cond_moves
    set_angle_target
    test_macro
    test_macro_2
    test_macro_3
    simple_loop
    loop_compose
    ))

;Novelty information:
;novelty archive contains a path average for each test case.
;each individual's paths are compared to their respective averages and an aggregate
;novelty score is generated.  The best scoring individual is selected.

(defn calc-avg-pt
  "averages a list of pts"
  [pts]
  (let [len (count pts)
        xlst (map first pts)
        xavg (/ (reduce +' xlst) len)
        ylst (map second pts)
        yavg (/ (reduce +' ylst) len)]
    (list (float xavg) (float yavg))))

(defn score-novelty
  "Takes an individual's paths and returns an aggregate score for those paths"
  [indiv-paths avg-paths]
  (let [calc-dist (fn [p1 p2] (let [xdif (utils/abs (-' (first p1) (first p2)))
                                    ydif (utils/abs (-' (second p1) (second p2)))]
                                    (Math/sqrt (+' (*' xdif xdif) (*' ydif ydif)))))]
    (reduce +'
      (map
        (fn [path average]
          (reduce +'
            (map #(calc-dist %1 %2) path average)))
        indiv-paths
        avg-paths))))

(def novelty-archive (ref '()))
;size limit
(def max-archive-size 500)
;number of add repeats
(def factor-scale 20)

(defn add-novel
  "take the equivalent of an individual test/path/pt list
   and add to novelty archive
   IN: path WITHOUT size associated"
  [machine-out]
  (dosync
    (alter novelty-archive conj (second (:novelty machine-out)))
    (commute novelty-archive #(take max-archive-size %))
    machine-out))

(def max-path-size
  ;get the average-size of an individual's paths
    (fn [best ind]
      (let [all-paths (:novelty ind)
            current (apply max (map count all-paths))]
        (if (> current best) current best))))

(defn normalize-length
  "Ensure lst is at least length elements long. This is accomplished by
   repeating the last element. If empty, do nothing."
  [lst length]
  (cond
    (empty? lst) (list)
    (< (count lst) length) (concat lst (repeat (- length (count lst)) (last lst)))
    :else lst))

(defn build-avg
  "IN: list of individuals' paths through each test map
   OUT: average paths for each test" ;; This needs more clarification
  [paths]
  ; ( (:indiv1 (:t1 (pt pt pt pt pt pt )) (:t2 (pt pt pt)) ... etc
  (let [prepped-list (map (fn [test-set]
                            (apply map vector test-set))
                            (apply map vector paths))]
        (map (fn [test-list] (map calc-avg-pt test-list)) prepped-list)))

(defn normalize-lengths
  "given an individuals path, change the size of that path to match the max
  by replicating the final element (stopped in place)"
  [goal path-set]
  (map #(normalize-length % goal) path-set))

(defn novelty-selection
  "select novel individual by comparing all individuals point paths
  against each other and an archive of novel individuals"
  [population]
  (dosync
    (let [goal-size (reduce max-path-size 0 population)
    ;(random-sample 0.5
          normalize-population (map #(assoc % :novelty (normalize-lengths goal-size (:novelty %))) population)
          all-paths (concat (map #(:novelty %) normalize-population) (deref novelty-archive))
          all-avg-paths (build-avg all-paths) ;length of number of test cases, contains path list for each
          ;relies on internal tranform that associates score with novelty field
          associate-score #(assoc % :novelty
                             (list (score-novelty (:novelty %) all-avg-paths) (:novelty %)))
          calc-best (fn [best-so-far next]
                            (if (> (first (:novelty next)) (first (:novelty best-so-far)))
                              next best-so-far))
          best (reduce calc-best (map associate-score normalize-population))]
          (repeatedly factor-scale (add-novel best)) ;; Causes side effects, updating the novelty archive
          (assoc best :novelty (second (:novelty best))))))


(def test-criteria
  ;constant multiples for each attribute of a machine run
  {:distance-from-target 1
   :total-crashes 0.2
   :moves-made 0.5})

(defn gradate-error
  [err]
  (let [logerr (Math/log err)]
    (Math/floor (* (/ logerr 10) logerr))))

(defn test-on-map
  "take movestack and location of map and run test"
  [mapfile]
  (let [maploaded (machine/data-structure-from-file mapfile)]
    (fn [movestack]
      (let [testresult (machine/test-instructions-list
                         movestack
                         maploaded
                         test-criteria)
            dist-from-target (:distance-from-target test-criteria)
            total-crashes (:total-crashes test-criteria)
            moves-made (:moves-made test-criteria)
            dist-to-target (:dist-to-target testresult)
            path-and-move-size (:path testresult)
            num-crash (:num-crash testresult)
            instr-total (:instr-total testresult)
            fitness (bigint (+' (*' dist-from-target dist-to-target)
                                (*' total-crashes num-crash)
                            ))]
      {:error (if (zero? (gradate-error fitness)) 0 fitness)
       :novelty path-and-move-size}))))

(defn set-exit-states-to-move-stack
  [ind]
  (assoc
    ind
    :exit-states
    (map
     #(:move %)
     (:exit-states ind))))

;; ; OLD mutation operator
;; ; e.g. #(mutation/refresh-youngest-genome %1 %2 2 %3)
;; (defn refresh-hottest-genome
;;   "Mutation operator that only mutates the hot genes."
;;   [new-gene mutate-percent min-keep-heat genome]
;;   (map
;;     #(if (or (hot? % min-keep-heat) (true-percent? mutate-percent)) (new-gene) %)
;;     genome))

(def test-maps
  (list
    ;"data/obsfiles/easytest.txt"
    "data/obsfiles/easytest2.txt"
    ;"data/obsfiles/test1.txt"
    ; "data/obsfiles/test2.txt"
    ; "data/obsfiles/test3.txt"
   ))

(def configuration
  {:genomic true
   :instructions instructions
   :literals (range 180)
   :inputses '(())
   :program-arity 0
   :testcases (map test-on-map test-maps)
   :max-generations 500
   :population-size 150
   :initial-percent-literals 0.5
   :max-initial-program-size 120
   :min-initial-program-size 100
   :evolution-config {:selection (list
                                  [65 novelty-selection]
                                  [15 #(selection/tournament-selection % 30)]
                                  [20 #(selection/epsilon-lexicase-selection % 30 10)])
                      :crossover (list
                                  [80 (hotspots/wrap
                                         #(crossover/alternation-crossover %1 %2 0.2 3))])
                      :deletion #((hotspots/wrap 
                                   (fn [g] (mutation/uniform-deletion %1 g))) %2)
                      :mutation #((hotspots/wrap
                                   (fn [g] (mutation/uniform-mutation %1 %2 g))) %3)
                      ; :mutation #(refresh-hottestest-genome %1 %2 2 %3)
                      :percentages '([45 :crossover]
                                     [15 :deletion]
                                     [15 :addition]
                                     [25 :mutation]
                                     [0 :copy])
                      :deletion-percent 10
                      :addition-percent 10
                      :mutation-percent 10
                      :decrease-heat-by-age true
                      :end-action #(do (machine/final-display % "data/obsfiles/easytest2.txt") (println %))
                      :individual-transform set-exit-states-to-move-stack
                      }})
