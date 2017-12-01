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
    (reduce +' (map (fn [path average]
      (reduce +' (map #(calc-dist %1 %2) path average))) indiv-paths avg-paths))))

;novelty archive contains a path average for each test-case
(def novelty-archive (ref '()))
;size limit for memory protection
(def max-archive-size 500)
(def factor-scale 5)

(defn add-novel
  "take the equivalent of an individual test/path/pt list and add to novelty archive
  IN: path WITHOUT size associated"
  [machine-out]
  (dosync
   ;repeat weights novel individual against average
    (alter novelty-archive conj (second (:novelty machine-out)))
    (commute novelty-archive #(take max-archive-size %))
    machine-out))

(defn build-avg
  "IN: list of individuals
   -each individual has a list of path lists (one for each test) containing pts
  OUT: average paths for each test"
  [paths]
  ; ( (:indiv1 (:t1 (pt pt pt pt pt pt )) (:t2 (pt pt pt)) ... etc
  (let [prepped-list (map (fn [test-set]
                            (apply map vector test-set))
                               (apply map vector paths))]
    (map (fn [test-list] (map calc-avg-pt test-list)) prepped-list)))

(defn novelty-selection
  "select novel individual by comparing all individuals ending locations against the ending locations
  in the archive"
  [population]
  (dosync
    (let [all-paths (concat (map (fn [ind] (:novelty ind)) population) (deref novelty-archive))
          all-avg-paths (build-avg all-paths)  ;length of number of test cases, contains path list for each
          ;relies on internal tranform that associates score with novelty field
          associate-score (fn [ind] (assoc ind :novelty (list (score-novelty (:novelty ind) all-avg-paths) (:novelty ind))))
          calc-best (fn [best-so-far next]
                            (if (< (first (:novelty next)) (first (:novelty best-so-far)))
                              next best-so-far))
          best  (reduce calc-best (map associate-score population))]
          (repeatedly factor-scale (add-novel best)) best)))


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
;;             fit (if (> 10 (:dist-to-target testresult)) (do (println testresult) 0) (:fitness testresult))]
;;       {:error fit
;;        :novelty (:end-loc testresult)}))))

;TODO: Generalize testcases field to problem. (Testcases currently lists map file location.  This is then loaded
; into the machine and run against an individual.  This generates an error map.)

(defn set-exit-states-to-move
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
                (test-on-map "data/obsfiles/test2.txt")
                (test-on-map "data/obsfiles/test3.txt"))
   :max-generations 500
   :population-size 200
   :initial-percent-literals 0.4
   :max-initial-program-size 100
   :min-initial-program-size 50
   :evolution-config {:selection (list
                                  [85 novelty-selection]
                                  ;[10 #(selection/tournament-selection % 30)]
                                  [15 #(selection/epsilon-lexicase-selection % 30 10)])
                                  [100 novelty-selection])
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
                      :end-action (fn [exit-state] (testing/final-display exit-state "data/obsfiles/easytest.txt"))
                      :individual-transform #(set-exit-states-to-move %)
                      }})
