(ns alloy.serialization
  (:use [taoensso.timbre])
  (:import [java.nio.charset Charset]))

;we can do better here, I think. I don't think
;(def ascii "The ASCII charset object" (Charset/forName "US-ASCII"))
;(def iso-latin-1 "The ISO Latin-1 charset object" (Charset/forName "ISO-8859-1"))
;(def utf-8 "The UTF-8 charset object" (Charset/forName "UTF-8"))
;(def utf-16-be "The UTF-16BE charset object" (Charset/forName "UTF-16BE"))
;(def utf-16-le "The UTF-16LE charset object" (Charset/forName "UTF-16LE"))
;(def utf-16 "The UTF-16 charset object" (Charset/forName "UTF-16"))
;(def default-charset "Default charset object of this JVM" (Charset/defaultCharset))
;(def target-charset "Usual charset for external calls" utf-8)

(def ascii "The ASCII charset" "US-ASCII")
(def iso-latin-1 "The ISO Latin-1 charset" "ISO-8859-1")
(def utf-8 "The UTF-8 charset" "UTF-8")
(def utf-16-be "The UTF-16BE charset" "UTF-16BE")
(def utf-16-le "The UTF-16LE charset" "UTF-16LE")
(def utf-16 "The UTF-16 charset" "UTF-16")
(def default-charset "Default charset of this JVM" (.name (Charset/defaultCharset)))
(def target-charset "Usual charset for external calls" utf-8)

(defrecord MimeType [primary-type sub-type])
(defn make-mime-type [sub-type & {:keys [primary-type] :or {primary-type "application"}}]
  (MimeType. primary-type sub-type))
(defn parse-mime-type [] ())

(defrecord ContentType [mime parameters])
(defrecord Serializer [serializer deserialzer])
(defn make-serializer [& args] (Serializer. args))

;Any Key -> Target Content type
(def content-type-mapping {})
(defn add-content-mapping [key content-type]
  {:pre [(some? key) (instance? ContentType content-type)]}
  (def content-type-mapping (assoc content-type-mapping key content-type)))

(add-content-mapping :json
  (ContentType. (make-mime-type "json") {:charset target-charset}))
(add-content-mapping :transit-json
  (ContentType. (make-mime-type "transit+json") {:charset target-charset}))

;MimeType or ContentType -> Serializer
(def global-content-converters
  "Global map of MimeType or ContentType -> Serializer.
  Used to map mime types to their serialization/deserialization mechanisms"
  {})

(defn validate-content-type [content-type]
  (assert (instance? ContentType content-type) (format "content-type %s is not a ContentType and was not found in content-type-mapping" content-type))
  content-type)

(defn convert-content-mapping [content-type-mapping content-type]
  (let [converted-content-type (get content-type-mapping content-type)]
    (info "Converted [" content-type "] to mapping [" converted-content-type "]")
    (if (some? converted-content-type)
      converted-content-type
      (validate-content-type content-type))))

(defn add-serializer
  ([serialization-target serializer]
   (def global-content-converters (add-serializer global-content-converters serialization-target serializer)))
  ([content-converters serialization-target serializer]
   (assoc content-converters serialization-target serializer)))

(defn serialize-base
  ""
  ([operation content-converters content-type payload]
   (let [content-type (convert-content-mapping content-type-mapping content-type)]
     (assert (nil? content-type) (format "Resolved content type %s was null"))
     (->> content-type
          ; Grab the content converter for this content type
          (get content-converters)
          ; Get the serializer or deserializer operation
          operation
          ; Apply the payload and content-type to the operation
          (#(% payload content-type))))
    ))

(defn serialize
  ([content-type content]
    (serialize-base :serializer global-content-converters content-type content))
  ([content-converters content-type content]
    (serialize-base :serializer content-converters content-type content)))

(defn deserialize
  ([content-type content]
   (serialize-base :deserialzer global-content-converters content-type content))
  ([content-converters content-type content]
   (serialize-base :deserialzer content-converters content-type content)))