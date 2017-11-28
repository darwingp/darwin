(ns darwin.problems.pathfinding
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.problems.pathfindingtests.machine :as testing])
  (:gen-class))

(def instructions
  '(new_angle
    ;set_speed
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
(def add-novel (fn [machine-out] (do (swap! novelty-archive conj (:novelty machine-out)) machine-out)))

(defn most-novel
  "Takes an individual and returns a tuple. Gets the max point"
  [indiv]
  ;TODO: make judgement about instructions length here?
  (reduce  ;take individual, get most novel
    (fn [novel loc]
      (if (and
        (> (first loc) (first novel))
        (> (second loc) (second novel))) loc novel))
    (:novelty indiv)))

(defn novelty-selection
  "select novel individual by comparing all individuals ending locations against the ending locations
  in the archive"
  [population]
  (let [pop-transform (map #(assoc % :novelty (most-novel %)) population)
        plus-archive (concat (map :novelty pop-transform) (deref novelty-archive))
        archive-size (count plus-archive)
        average-x (/ (reduce (fn [prev new] (+ (first prev) (first new))) plus-archive) archive-size)
        average-y (/ (reduce (fn [prev new] (+ (second prev) (second new))) plus-archive) archive-size)
        average-size (/ (reduce (fn [prev new] (+ (nth prev 2) (nth new 2))) plus-archive) archive-size)
        distance (fn [pt]
           (let [xdif (- average-x (first pt)) ydif (- average-y (second pt))]
           (Math/sqrt (+ (* xdif xdif) (* ydif ydif)))))]
        ;find longest distance from average (includes archived anomolies)
        (add-novel
        (reduce
          (fn [longest-indiv next-indiv]
            (if (and (> (distance (:novelty next-indiv))
                   (distance (:novelty longest-indiv))) (> average-size (nth (:novelty next-indiv) 2))) next-indiv longest-indiv))
          pop-transform))))

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
      (let [testresult (testing/test-instructions-list movestack maploaded test-criteria)]
;        (println testresult)
      {:error (:fitness testresult)
       :novelty (:end-loc testresult)}))))

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
   :behavioral-diversity (fn [_] -1)
   ;; :behavioral-diversity #(do
   ;;                          (println %)
   ;;                            (testing/calculate-behavior-div % 5)) ; TODO: play with the frame
   :max-generations 500
   :population-size 200
   :initial-percent-literals 0.4
   :max-initial-program-size 100
   :min-initial-program-size 50
   :evolution-config {:selection novelty-selection
                      :crossover #(crossover/alternation-crossover %1 %2 0.2 6)
                      :percentages '([40 :crossover]
                                     [25 :deletion]
                                     [5 :addition]
                                     [30 :mutation])
                      :deletion-percent 7
                      :addition-percent 7
                      :mutation-percent 7
                      :keep-test-attribute :novelty
                      :individual-transform (fn [ind] (assoc ind :exit-states (map #(:move %) (:exit-states ind))))
                      }})
