(ns darwin.cross-test
  (:require [clojure.test :refer :all])
  (:require [darwin.gp.crossover :as crossover]))

;(require '[clojure.test.check.generators :as gen]) ; (Note: this is not working)
(def test-a (range 20))
(def test-b '(:a :b :c :d :e :f :g :h :i :j :k :l :m))

;TODO: make tests on noise and contents of lists (maximize usefulness of crossover)

(deftest test-generation-creation
    (testing "(alternation-crossover)"
      (is
        (<= (count (crossover/alternation-crossover
       test-a test-b 0.2 10))
      100 ))))
