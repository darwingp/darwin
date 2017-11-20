(ns darwin.problems.pathfinding
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.problems.pathfindingtests.machine :as testing])
  (:gen-class))

(def instructions
  '(new_angle
    set_speed
    new_cond_moves
    set_angle_target
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

(def novelty-archive (atom '()))
(def add-novel (fn [machine-out] (do (swap! novelty-archive conj machine-out) machine-out)))

(defn novelty-selection
  "select novel individual by comparing all individuals ending locations against the ending locations
  in the archive"
  [population]
  (let [population-locations (map (fn [indiv] (:end-loc (first (:exit-states indiv)))))
        plus-archive (concat population-locations (deref novelty-archive))
        average-x (reduce (fn [prev new] (+ (first prev) (first new))) plus-archive)
        average-y (reduce (fn [prev new] (+ (second prev) (second new))) plus-archive)
        distance (fn [pt]
          (let [xdif (- average-x (first pt)) ydif (- average-y (second pt))]
          (Math/sqrt (+ (* xdif xdif) (* ydif ydif)))))]
        ;find longest distance from average (includes archived anomolies)
        (add-novel
        (reduce
          (fn [longest-indiv next-indiv]
            (if (> (distance (:end-loc (first (:exit-states longest-indiv))))
                   (distance (:end-loc (first (:exit-states next-indiv))))) longest-indiv next-indiv))
          (first population-locations) population-locations))))

(def test-criteria
  ;constant multiples for each attribute of a machine run
  {:distance-from-target 0.8
   :total-crashes 0.5
   :moves-made 2})

(defn test-on-map
  "take movestack and location of map and run test"
  [map]
  (let [maploaded (testing/load-obstacle-list map)]
    (fn [movestack]
      (:fitness (testing/test-instructions-list
      movestack maploaded test-criteria)))))


;TODO: Generalize testcases field to problem. (Testcases currently lists map file location.  This is then loaded
; into the machine and run against an individual.  This generates an error map.)
(def configuration
  {:genomic true
   :instructions instructions
   :literals (range 180)
   :inputses '(())
   :program-arity 0
   :testcases (list
                (test-on-map "data/obsfiles/test1.txt")
                (test-on-map "data/obsfiles/test2.txt"))
                ;(test-on-map "data/obsfiles/test3.txt")) ;; This should be a list of functions which take a final push state and returns a fitness.
   :behavioral-diversity #(testing/calculate-behavior-div % 5) ; TODO: play with the frame
   :max-generations 500
   :population-size 200
   :initial-percent-literals 0.6
   :max-initial-program-size 100
   :min-initial-program-size 50
   :evolution-config {:selection #(selection/lexicase-selection % 1) ;novelty-selection
                      :crossover #(crossover/alternation-crossover %1 %2 0.2 6)
                      :percentages '([50 :crossover]
                                     [20 :deletion]
                                     [10 :addition]
                                     [20 :mutation])
                      :deletion-percent 7
                      :addition-percent 7
                      :mutation-percent 7
                      :individual-transform (fn [ind] (assoc ind :exit-states (map #(:move %) (:exit-states ind))))
                      }})
