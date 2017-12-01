(ns darwin.problems.pathfinding
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.mutation :as mutation])
  (:require [darwin.problems.pathfindingtests.machine :as testing])
  (:gen-class))

(def instructions
  '(new_angle
    new_angle
    new_angle
    new_angle
    ;set_speed
    new_cond_moves
    set_angle_target
    loop_moves
    while_moves
    loop_moves
    while_moves
    loop_moves
    while_moves
    loop_moves
    while_moves
    test_macro
    ;move-dup
    ;integer-dup
    ;integer-frombool
    ;boolean-and
    ;boolean-or
    ;exec-if
    ;exec-dup
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
  (let [calc-dist (fn [p1 p2] (let [xdif (Math/abs (-' (first p1) (first p2)))
                                    ydif (Math/abs (-' (second p1) (second p2)))]
                                    (Math/sqrt (+' (*' xdif xdif) (*' ydif ydif)))))]
    (reduce +'
      (map
        (fn [path average]
          (reduce +'
            (map #(calc-dist %1 %2) path average)))
        indiv-paths
        avg-paths))))

;novelty archive contains a path average for each test-case
(def novelty-archive (ref '()))
;size limit for memory protection
(def max-archive-size 500)
(def factor-scale 20)

(defn add-novel
  "take the equivalent of an individual test/path/pt list and add to novelty archive
  IN: path WITHOUT size associated"
  [machine-out]
  (dosync
   ;repeat weights novel individual against average
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

          ;; TODO: include novelty archive in all-avg-paths calculation.
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
  [map]
  (let [maploaded (testing/load-obstacle-list map)]
    (fn [movestack]
      (let [testresult (testing/test-instructions-list movestack maploaded test-criteria)
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
  (assoc ind :exit-states (map #(:move %) (:exit-states ind))))

(def configuration
  {:genomic true
   :instructions instructions
   :literals (range 90)
   :inputses '(())
   :program-arity 0
   :testcases (list
                (test-on-map "data/obsfiles/easytest.txt")
                (test-on-map "data/obsfiles/easytest2.txt")
                (test-on-map "data/obsfiles/test1.txt")
                ;(test-on-map "data/obsfiles/test2.txt")
                ;(test-on-map "data/obsfiles/test3.txt")
                )
   :max-generations 500
   :population-size 300
   :initial-percent-literals 0.5
   :max-initial-program-size 120
   :min-initial-program-size 100
   :evolution-config {:selection (list
                                  [75 novelty-selection]
                                  [10 #(selection/tournament-selection % 30)]
                                  [15 #(selection/epsilon-lexicase-selection % 30 10)])
                      :crossover (list
                                  [100 (crossover/age-hotspot-wrap
                                         #(crossover/alternation-crossover %1 %2 0.2 7))])
                      :mutation #(mutation/refresh-youngest-genome %1 %2 2 %3)
                      :percentages '([55 :crossover]
                                     [15 :deletion]
                                     [15 :addition]
                                     [15 :mutation]
                                     [0 :copy])
                      :deletion-percent 7
                      :addition-percent 7
                      :mutation-percent 7
                      :keep-test-attribute :novelty
                      :end-action #(do (testing/final-display % "data/obsfiles/easytest2.txt") (println %))
                      :individual-transform set-exit-states-to-move-stack
                      }})
