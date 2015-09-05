(ns alloy.transit.extensions
  (:use [clojurewerkz.support.bytes])
  (:import [java.io ByteArrayOutputStream])
  (:require [cognitect.transit :as transit]
            [alloy.serialization :as serialization]))

(defn to-transit-byte-array [transit-type, charset, input]
  (def out (ByteArrayOutputStream. 4096))
  (def writer (transit/writer out transit-type))
  (transit/write writer input)
  (.toByteArray out))

(defn from-transit-byte-array [transit-type, charset, input]
  (def out (ByteArrayOutputStream. 4096))
  (def writer (transit/writer out transit-type))
  (transit/write writer input)
  (.toByteArray out))

(defn transit-serializer [transit-type]
  (serialization/make-serializer
    (fn [content content-type]
     (to-transit-byte-array transit-type (-> content-type :parameters :charset) content))
    (fn [bytes content-type]
     (from-transit-byte-array transit-type (-> content-type :parameters :charset) bytes))))