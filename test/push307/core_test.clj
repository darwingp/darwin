(ns push307.core-test
  (:require [clojure.test :refer :all]
            [clojure.data.generators :as gen]
            [clojure.test.generative :as test :refer (defspec)]
            [clojure.test.generative.runner :as runner]
            [push307.core :as core]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generators for Push data structures

(defn gen-inputs
  [n]
  (zipmap (map #(str "in" (+ 1 %)) (range n)) (gen/list gen/int)))

(defn gen-exec
  "Returns a random instruction"
  []
  (nth
    core/instructions
    (rand-int (count core/instructions))))

(defn gen-push-state
  "Returns a random push state."
  []
  {:exec (gen/list gen-exec)
   :integer (gen/list gen/int)
   :string (gen/list gen/string)
   :boolean (gen/boolean)
   :input (gen-inputs 3)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TESTS

(deftest test-push-to-stack
  (testing "pushing to the stack"
    (let [state { :integer '(1 2) }
          stack :integer
          item 0
          state-after (core/push-to-stack state stack item)]
      (is
        (= 
          (stack state-after)
          '(0 1 2))))))

(deftest test-push-many-to-stack
  (testing "pushing many to the stack"
    (let [state { :integer '(2 3) }
          stack :integer
          items '(1 0)
          state-after (core/push-many-to-stack state stack items)]
      (is
        (=
          (stack state-after)
          '(0 1 2 3))))))

(deftest test-pop-stack
  (testing "popping elements from the stack"
    (let [state { :integer '(0 1 2 3) }
          stack :integer
          state-after (core/pop-stack state stack)]
      (is (=
            (stack state-after)
            '(1 2 3))))))

(deftest test-pop-n-stack
  (testing "popping an arbitrary number of elements from the stack"
    (let [state { :integer '(0 1 2 3 4 5) }
          stack :integer
          state-after (core/pop-n-stack state stack 2)]
      (is
        (=
          (stack state-after)
          '(2 3 4 5))))))          

(deftest test-peek-stack
  (testing "peeking the top element of the stack"
    (let [state { :integer '(5 2) }
          stack :integer
          peeked (core/peek-stack state stack)]
      (is
        (= peeked 5)))))

(deftest test-empty-stack?
  (testing "the empty stack predicate"
    (let [state-empty { :integer '() }
          state-nonempty { :integer '(1 2) }
          stack :integer
          v-empty (core/empty-stack? state-empty stack)
          v-nonempty (core/empty-stack? state-nonempty stack)]
      (is
        (= v-empty true))
      (is
        (= v-nonempty false)))))

;; TODO: figure out how to use me!
;; this is an example of a generative test from
;; https://blog.ochronus.com/generative-testing-in-clojure-fce73ec3d25f
;(defspec longs-are-closed-under-inc
;  inc ; func
;  [^long 1] ; args
;  (assert (instance? Long %))) ; assertions
;; I (nate) do not know how to run the generative tests
