(ns darwin.problems.pathfinding
  (:require [darwin.gp.selection :as selection])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.gp.crossover :as crossover])
  (:require [darwin.problems.pathfindingtests.machine :as testing])
  (:gen-class))

(def instructions
  '(new_move
    new_angle))

(defn run-machine
  "takes individual, runs machine on individual"
  [indiv]
  )

;TODO: Generalize testcases field to problem. (Testcases currently lists map file location.  This is then loaded
; into the machine and run against an individual.  This generates an error map.)
(def configuration
  {:genomic true
   :instructions instructions
   :literals '(1 2 3 4) ;; TODO: changeme?
   :inputses '(()) ;TODO: this needs to change
   :program-arity 0
   :testcases (testing/load-obstacle-list "data/obsfiles/test1.txt") ;; This should be a list of functions which take a final push state and returns a fitness.
   :behavioral-diversity #(calculate-behavior-div % 5) ; TODO: play with the frame
   :max-generations 500
   :population-size 200
   :initial-percent-literals 0.2
   :max-initial-program-size 100
   :min-initial-program-size 50
   :evolution-config {:selection #(selection/tournament-selection % 30)
                      :crossover crossover/uniform-crossover
                      :percentages '([60 :crossover]
                                     [10 :deletion]
                                     [10 :addition]
                                     [20 :mutation])
                      :deletion-percent 7
                      :addition-percent 7
                      :mutation-percent 7
                      }})
