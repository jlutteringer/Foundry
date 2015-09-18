(ns alloy.core.core
  (:require [alloy.core.string :as string]
            [alloy.core.util :as util]))

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
(defn arg-expand-vector-field [field-vector]
  (cond (= (count field-vector) 1) {:label (first field-vector)}
        (= (count field-vector) 2)
          (if (= (second field-vector) '_)
            {:label (first field-vector)}
            {:label (first field-vector) :default (second field-vector)})
        (= (count field-vector) 3)
          (if (= (second field-vector) '_)
            {:label (first field-vector) :validator (get field-vector 2)}
            {:label (first field-vector) :default (second field-vector) :validator (get field-vector 2)})))

(defn parse-arg [field]
  {:post [(-> % :label symbol?)]}
  (cond (symbol? field) {:label field}
        (map? field) field
        (vector? field) (arg-expand-vector-field field)))

(def default-arg? #(contains? % :default))
(def required-arg? #(not (default-arg? %)))

(defn required-args [args]
  (filter required-arg? args))

(defn default-args [args]
  (filter default-arg? args))

(defn required-arg-positions [expanded-fields]
  (map first (filter
               #(required-arg? (second %))
               (map-indexed (fn [i field] [i field]) expanded-fields))))

(defn arg-defaults-map [args]
  (let [default-arg-list (default-args args)]
    (if (empty? default-arg-list)
      {}
      (apply
        #(hash-map (keyword (:label %)) (:default %))
        (default-args args)))))

;(partition 2 (conj positions -1))
(defn record-apply-default-args [args expanded-fields]
  (let [args (into [] args) required-fields (into [] (required-args expanded-fields))]
    (cond
      (< (count args) (count required-fields))
        (util/assert-error "Insufficient number of args " args " for required fields "
          (map :label required-fields))
      (= (count args) (count required-fields))
        (util/position-merge
          (map vector args (required-arg-positions expanded-fields))
          (map :default (filter default-arg? expanded-fields)))
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
        '~(symbol (str *ns* "/->" record-token))))

(defn record-make-constructor-macro [record-token expanded-fields]
  `(defmacro
        ~(symbol (str "->" (string/convert-format (name record-token) :dash-lowercase)))
        ~'[& args]
        ~(record-make-constructor-macro-body record-token expanded-fields)))

(defmacro record-make-primary-constructor [record-token fields]
  (record-make-constructor-macro record-token (into [] (map parse-arg fields))))

(defn record-make-map-constructor [record-token fields]
  `(defn
     ~(symbol (str "map->" (string/convert-format (name record-token) :dash-lowercase)))
     ~'[fieldMap]
      (~(symbol (str "map->" record-token))
        (merge ~(arg-defaults-map (map parse-arg fields)) ~'fieldMap))))

; (record TestRecord [a [b "b"] c [d "d"] {:label d :default "d" :validator validation-fn}])

; (->test-record "a" "c") => (->TestRecord "a" "b" "c" "d")
; (->test-record "a" "b" "c")
; (->test-record "a" _ "c" "d")
; (map->test-record a "a" b "b" d "d")

(defmacro record [record-token fields]
  `(do
     (record-make-definition ~record-token ~fields)
     (record-make-primary-constructor ~record-token ~fields)))

;(record-make-map-constructor ~record-token ~fields)