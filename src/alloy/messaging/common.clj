(ns alloy.messaging.common
  (:require [alloy.serialization :as serialization]
            [alloy.transit.extensions :as [transit-extensions]]))

(serialization/add-serializer
  (serialization/make-mime-type "transit+json") (transit-extensions/transit-serializer :json))

(def content-converters
  {"application/transit+json" (transit-extensions/transit-serializer :json)
   })

(def convert-content serialization/convert-content)