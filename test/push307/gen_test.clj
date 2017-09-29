(ns push307.gen-test
  (:require [clojure.test :refer :all]) 
  (:require [push307.pushgp.generation :as generation]))

;(require '[clojure.test.check.generators :as gen])

(deftest test-generation-creation
  (testing "(generate-random-program '(in1 in2
    integer_+
    integer_-
    integer_*
    integer_%
    integer_>
    integer_<) (range 10) (gen/choose 1 100)"
    (is
      (<= (count (generation/generate-random-program  generation/instructions 
      (range 10) 25))
    25 ))))
;(gen/choose 1 100)
