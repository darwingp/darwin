(ns push307.push-test
  (:require [clojure.test :refer :all])
  (:require [push307.push.instructions :refer :all])
  (:require [push307.push :as push]))

(deftest test-ordered-instructions
  (testing "(2 1 integer_- 1 integer_-)"
    (is
      (=
        (:integer (push/interpret-push-program `(2 1 integer_- 1 integer_-) push/empty-push-state))
        '(0))))
  (testing "(1 5 integer_<)"
    (is
      (=
        (:boolean (push/interpret-push-program `(1 5 integer_<) push/empty-push-state))
        '(true))))
  (testing "(5 1 integer_>)"
    (is
      (=
        (:boolean (push/interpret-push-program `(5 1 integer_>) push/empty-push-state))
        '(true)))))
        
