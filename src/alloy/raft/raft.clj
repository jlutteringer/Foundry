(ns alloy.raft.raft
  (:require [alloy.core :refer :all]
            [alloy.util :as util]
            [alloy.time :as time]
            [org.httpkit.server :as http-server]
            [org.httpkit.client :as http]
            [clojure.core.async :as async]
            [taoensso.timbre :refer :all]
            [compojure.handler :only [site]]
            [compojure.core :as c]))

(record RaftCluster
  [[addresses []]])

(record RaftServer
  [address
   [uuid (util/uuid)]
   [cluster (->raft-cluster)]
   [heartbeat-interval (time/->duration 300)]
   [election-timeout [(time/->duration 150) (time/->duration 300)]]])

(record RaftServerState [config [status (ref :follower)] [term (ref 0)]])

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})

(defn start-election-timeout [status-update-chan range]
  (let [timeout-value
        (apply util/rand-between (map #(time/amount % :miliseconds) range))
        cancel-chan (async/chan)]
    (async/go
      (let [[_ chan] (async/alts! [cancel-chan (async/timeout timeout-value)])]
        (if (= chan cancel-chan)
          (do
            (info "Election timeout cancelled"))
          (do
            (info "Election timed out after" timeout-value "Becoming a canidate...")
            (async/>! status-update-chan :canidate)))))))

(defn start-status-update-listener [status-ref begin-term-chan]
  (let [status-update-chan (async/chan)]
    (async/go
      (while true
        (let [new-status (async/<! status-update-chan)]
          (info "Recieved updated status" new-status "for node")
          (dosync (ref-set status-ref new-status))
          (cond
            (= new-status :canidate) (async/>! begin-term-chan "")))))
    status-update-chan))

(defn start-begin-term-listener [server]
  (let [begin-term-chan (async/chan)]
    (async/go
      (while true
        ))
    begin-term-chan))

(defn request-votes [server]
  (map #() ))

(defn make-request-vote-handler []
  (fn [request]
    (info "We made it")
    (http-server/with-channel request channel
      (info "We made it 2")
      (print request))))

; {term: 0}
(defn build-server-definition [request-vote-handler]
  (c/defroutes routes
    (c/POST "/raft/request-vote" [] request-vote-handler)))

(defn start [server]
  (let [server (->raft-server-state server)
        begin-term-chan (start-begin-term-listener server)
        status-update-chan (start-status-update-listener (:status server) begin-term-chan)
        cancel-election-timeout-chan
          (start-election-timeout status-update-chan (-> server :config :election-timeout))
        server-definition (build-server-definition (make-request-vote-handler))]
    ;(start-election-timeout status-update-chan (:election-timeout server))

    (http-server/run-server server-definition (-> server :config :address :port))
    (info "Server started")))

;  (http-server/run-server app {:port (-> server :address :port)})