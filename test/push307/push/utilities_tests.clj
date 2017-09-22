(ns push307.push.utilities-tests
  (:require [clojure.test :refer :all]
            [push307.push.utilities :as utilities]))

(deftest test-push-to-stack
  (testing "pushing to the stack"
    (let [state { :integer '(1 2) }
          stack :integer
          item 0
          state-after (utilities/push-to-stack state stack item)]
      (is
        (= 
          (stack state-after)
          '(0 1 2))))))

(deftest test-push-many-to-stack
  (testing "pushing many to the stack using a list"
    (let [state { :integer '(2 3) }
          stack :integer
          items '(1 0)
          state-after (utilities/push-many-to-stack state stack items)]
      (is
        (=
          (stack state-after)
          '(0 1 2 3)))))
  (testing "pushing many to the stack using a vector"
    (let [state { :integer '(2 3) }
          stack :integer
          items [1 0]
          state-after (utilities/push-many-to-stack state stack items)]
      (is
        (=
          (stack state-after)
          '(1 0 2 3))))))

(deftest test-pop-stack
  (testing "popping elements from the stack"
    (let [state { :integer '(0 1 2 3) }
          stack :integer
          state-after (utilities/pop-stack state stack)]
      (is (=
            (stack state-after)
            '(1 2 3))))))

(deftest test-pop-n-stack
  (testing "popping an arbitrary number of elements from the stack"
    (let [state { :integer '(0 1 2 3 4 5) }
          stack :integer
          state-after (utilities/pop-n-stack state stack 2)]
      (is
        (=
          (stack state-after)
          '(2 3 4 5))))))          

(deftest test-peek-stack
  (testing "peeking the top element of the stack"
    (let [state { :integer '(5 2) }
          stack :integer
          peeked (utilities/peek-stack state stack)]
      (is
        (= peeked 5)))))

(deftest test-empty-stack?
  (testing "the empty stack predicate"
    (let [state-empty { :integer '() }
          state-nonempty { :integer '(1 2) }
          stack :integer
          v-empty (utilities/empty-stack? state-empty stack)
          v-nonempty (utilities/empty-stack? state-nonempty stack)]
      (is
        (= v-empty true))
      (is
        (= v-nonempty false)))))

(deftest test-push-return-stack
  (testing "returning normal return value to the stack"
    (let [state { :integer '(2 3) }
          new-state (utilities/push-return-stack state :integer 1)]
      (is (= (:integer new-state) '(1 2 3)))))
  (testing "returning multiple values to the stack" :STUB)
  (testing "returning multiple values to different stacks" :STUB))
