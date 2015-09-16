(ns alloy.core-test
  (:require [clojure.test :refer :all]
            [alloy.core :refer :all]))

;(deftest test-record-macro
;  (testing "Simple Record creation"
;    (record TestRecord [])
;    (->test-record)))

(record TestRecord [a])
(->test-record "a")