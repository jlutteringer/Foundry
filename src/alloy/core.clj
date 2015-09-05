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
(defrecord Argument [label default validator])

(defn arg-expand-vector-field [field-vector]
  (cond (= (count field-vector) 1) (->Argument (first field-vector) nil nil)
        (= (count field-vector) 2)
          (if (= (second field-vector) '_)
            (->Argument (first field-vector) nil nil)
            (->Argument (first field-vector) (second field-vector) nil))
        (= (count field-vector) 3)
          (if (= (second field-vector) '_)
            (->Argument (first field-vector) nil  (get field-vector 2))
            (->Argument (first field-vector) (second field-vector) (get field-vector 2)))))

(defn parse-arg [field]
  {:post [(-> % :label symbol?)]}
  (cond (symbol? field) {:label field}
        (map? field) field
        (vector? field) (arg-expand-vector-field field)))

(def default-arg? #(contains? % :default))
(def required-arg-predicate #(not (default-arg? %)))
(defn required-args [args]
  (filter required-arg-predicate args))

(defn required-arg-positions [expanded-fields]
  (map first (filter
               #(required-arg-predicate (second %))
               (map-indexed (fn [i field] [i field]) expanded-fields))))

;["a" "c"]
;[0 2]
;["b" "d"]
;["a" "b" "c" "d"]
(defn position-merge [values positions target]
  ())

;(partition 2 (conj positions -1))

(defn record-apply-default-args [args expanded-fields]
  (let [args (into [] args) required-fields (into [] (required-args expanded-fields))]
    (cond
      (< (count args) (count required-fields))
        (util/assert-error "Insufficient number of args " args " for required fields "
          (map :label required-fields))
      (= (count args) (count required-fields))
        (let [required-field-positions (required-arg-positions expanded-fields)]
          (map-indexed ))
        (map-indexed
          #(if (record-field-default? %2)
            (:default %2)
            (get args %1)) expanded-fields)
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
  (record-make-constructor-macro record-token (into [] (map parse-arg fields))))

; (record TestRecord [a [b "b"] c [d "d"] {:label d :default "d" :validator validation-fn}])

; (->test-record "a" "c") => (->TestRecord "a" "b" "c" "d")
; (->test-record "a" "b" "c")
; (->test-record "a" _ "c" "d")
; (map->test-record a "a" b "b" d "d")

(defmacro record [record-token fields]
  `(do
     (record-make-definition ~record-token ~fields)
     (record-make-constructor ~record-token ~fields)))