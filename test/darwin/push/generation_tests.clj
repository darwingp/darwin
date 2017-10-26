(ns darwin.push.generation-tests
  (:require [clojure.test :refer :all])
  (:require [darwin.push.generation :as generation])
  (:require [darwin.push.instructions :as instructions])
  (:gen-class))

;(require '[clojure.test.check.generators :as gen])

(deftest test-generation-creation
  (testing "(generate-random-program instructions/all (range 10) (gen/choose 1 100)"
    (is
      (<= (count
            (generation/generate-random-program instructions/all
                                                (range 10)
                                                0.2
                                                25
                                                10))
    25))))
