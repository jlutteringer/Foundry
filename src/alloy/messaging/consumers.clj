(ns alloy.messaging.consumers
  (:require [langohr.basic :as messaging]
            [alloy.transit.extensions :refer [to-transit-byte-array]]
            [clojurewerkz.support.bytes :refer [to-byte-array]]))

(defn subscribe
  [channel queue callback options]
  ())