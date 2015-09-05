(ns alloy.core
  (:require [alloy.string :as string]
            [alloy.util :as util]))

(defn record-extract-fields [fields]
  (map #(if (symbol? %) % (first %)) fields))

(defmacro record-make-definition [record-name fields]
  `(defrecord ~record-name ~(-> fields record-extract-fields vec)))

; Arg Forms:
; a
; [a]
; [a "a"]
; [a "a" validate-a]
; [a _ validate-a]
; Expanded Form:
; {:label a :default "a" :validator validate-a}
(defn record-expand-vector-field [field-vector]
  (cond (= (count field-vector) 1) {:label (first field-vector)}
        (= (count field-vector) 2)
          (if (= (second field-vector) '_)
            {:label (first field-vector)}
            {:label (first field-vector) :default (second field-vector)})
        (= (count field-vector) 3)
          (if (= (second field-vector) '_)
            {:label (first field-vector) :validator (get field-vector 2)}
            {:label (first field-vector) :default (second field-vector) :validator (get field-vector 2)})))

(defn record-expand-field [field]
  {:post [(-> % :label symbol?)]}
  (cond (symbol? field) {:label field}
        (map? field) field
        (vector? field) (record-expand-vector-field field)))

(defn record-get-required-fields [expanded-fields]
  (filter #(not (contains? % :default)) expanded-fields))

(defn record-apply-default-args [args expanded-fields]
  (let [args (into [] args) required-fields (into [] (record-get-required-fields expanded-fields))]
    (cond
      (< (count args) (count required-fields))
        (util/assert-error "Insufficient number of args " args " for required fields "
          (map :label required-fields))
      (= (count args) (count required-fields))
        (map-indexed
          #(if (contains? %2 :default)
            (:default %2)
            (get args %1)) expanded-fields)  ;we need to take into account specifying just required args here...
      :else
        (map-indexed
          #(let [arg (get args %1)]
            (if
              (or (nil? arg) (= arg '_))
              (:default %2)
              arg))
          expanded-fields))))

(defn record-make-constructor-macro-body [record-token expanded-fields]
  `(conj
        (record-apply-default-args ~'args '~expanded-fields)
        '~(symbol (str "->" record-token))))

(defn record-make-constructor-macro [record-token expanded-fields]
  `(defmacro
        ~(symbol (str "->" (string/convert-format (name record-token) :dash-lowercase)))
        ~'[& args]
        ~(record-make-constructor-macro-body record-token expanded-fields)))

(defmacro record-make-constructor [record-token fields]
  (record-make-constructor-macro record-token (into [] (map record-expand-field fields))))

; (record TestRecord [a [b "b"] c [d "d"] {:label d :default "d" :validator validation-fn}])

; (->test-record "a" "c") => (->TestRecord "a" "b" "c" "d")
; (->test-record "a" "b" "c")
; (->test-record "a" _ "c" "d")
; (map->test-record a "a" b "b" d "d")

(defmacro record [record-token fields]
  `(do
     (record-make-definition ~record-token ~fields)
     (record-make-constructor ~record-token ~fields)))