(ns alloy.messaging.producers
  (:require [langohr.basic :as messaging]
            [alloy.messaging.common :as messaging-common]))

(defn publish [channel exchange queue payload {:keys [content-type] :as options}]
  (messaging/publish channel exchange queue (messaging-common/convert-content payload content-type) options))