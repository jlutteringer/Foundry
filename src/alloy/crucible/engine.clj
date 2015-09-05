(ns alloy.crucible.engine
  (:require [langohr.core :as core]
            [langohr.channel :as channel]
            [langohr.queue :as queue]
            [langohr.consumers :as consumers]
            [alloy.messaging.producers :as producers]))

(def jobs {})

(defn find-job [key]
  (key jobs))

(defn create-job [description job]
  {:context {:description description} :job job})

(defn register-job [description job]
  (def jobs
    (assoc jobs (:key description) (create-job description job))))

(defn queue-job [key arguments]
  (let [job (if (keyword? key) (key jobs) key)]
    ((:job job) (:context job) arguments)))

(def ^{:const true} default-exchange-name "")
(def ^{:const true} jobs-process-queue "jobs.process")

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                   (String. payload "UTF-8")
                   delivery-tag
                   content-type
                   type)))

(defn start-job-process-consumer
  "Starts a consumer in a separate thread"
  [ch queue-name]
  (consumers/subscribe ch queue-name message-handler {:auto-ack true}))

(defn start-rabbitmq []
  (let [connection (core/connect)
        channel (channel/open connection)
        queue-name jobs-process-queue]
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber channel)))
    (queue/declare channel queue-name {:exclusive false :auto-delete true})
    (start-job-process-consumer channel queue-name)
    (println "[main] Publishing...")
    (producers/publish channel default-exchange-name queue-name {:value "Hello World"} {:content-type "application/transit+json" :type "greetings.hi"})))

;application/transit+msgpack