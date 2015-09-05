(ns alloy.crucible.driver
  (:use [alloy.crucible.engine]
        [alloy.transit.extensions]
        [taoensso.timbre]
        [clojurewerkz.support.bytes]))

(register-job
  {:key :testJob
   :name "Test Job"}
  (fn [jobContext arguments]
    (info "Starting" (:name jobContext) "job with params " arguments)))

;(register-job 
;    {:key :testJob2
;     :name "Test Job 2"} 
;    (fn [jobContext arguments] 
;      (info "Starting" (:name jobContext) "job with params " arguments)))
;
;(def testJob3 
;  (create-job
;    {:key :testJob2
;     :name "Test Job 2"}
;    (fn [jobContext arguments] 
;      (info "Starting" (:name jobContext) "job with params " arguments))))

(defn main []
  (queue-job :testJob {:param1 "param1" :param2 "param2"}))