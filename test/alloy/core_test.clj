(ns alloy.core-test
  (:require [clojure.test :refer :all]
            [alloy.core :refer :all]))

(record TestRecord [a])
(record TestRecord2 [])
(record TestRecord3 [a [b "b"]])
;map constructor for this definition fails
(record TestRecord4 [a [b "b"] c [d "d"]])
(record TestRecord5 [a])
(record TestRecord6 [a])
(record TestRecord7 [a])
(record TestRecord8 [a])

(deftest test-record-macro
  (testing "Simple Record creation"
    (is (= (->TestRecord "a") (->test-record "a"))))
  (testing "Empty Record creation"
    (is (= (->TestRecord2) (->test-record-2))))
  (testing "Default Values"
    (is (= (->TestRecord3 "a" "b") (->test-record-3 "a")))
    (is (= (->TestRecord3 "a" 2) (->test-record-3 "a" 2)))
    (is (= (->TestRecord4 "a" "b" "c" "d") (->test-record-4 "a" "c")))
    (is (= (->TestRecord4 "a" "b" "c" 4) (->test-record-4 "a" _ "c" 4)))
    (is (= (->TestRecord4 "a" 2 "c" 4) (->test-record-4 "a" 2 "c" 4)))))