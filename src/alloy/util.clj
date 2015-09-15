(ns alloy.util
  (:import (java.util.regex Pattern)))

(def pattern? #(instance? Pattern %))

(defn fn-rest [delegate]
  (fn [& args] (apply delegate (rest args))))

(defn find-first
  [f coll]
  (first (filter f coll)))

(defn take-even [x]
  (take-nth 2 x))

(defn take-odd [x]
  (take-nth 2 (drop 1 x)))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn gen-symbol
  ([]
   (gen-symbol "_"))
  ([prefix]
   (-> prefix (str (uuid)) symbol)))

(defn countf [f c]
  (->> c (filter f) count))

(def curry-blank? #(= % '_))

(defn- curry-gather-args [args]
  (let [symbols (map #(if (curry-blank? %) (gen-symbol) nil) args)]
    [(filter (comp not nil?)  symbols)
     (map (partial reduce (fn [arg symbol] (if (curry-blank? arg) symbol arg)))
          (map vector args symbols))]))

(defmacro curry [func & args]
  (let [gathered-args (curry-gather-args args)]
    (list 'fn (into [] (first gathered-args))
          (conj (second gathered-args) func))))

(defn assert-error [& messages]
  (throw (new AssertionError (apply pr-str messages))))

(defn position-merge-helper [values target result]
  (cond (empty? values) (concat result target)
        (empty? target) (concat result (map first values))
        :else
        (if (zero? (second (first values)))
          (recur (map #(vector (first %) (dec (second %))) (rest values))
                 target
                 (conj result (first (first values))))
          (recur (map #(vector (first %) (dec (second %))) values)
                 (rest target)
                 (conj result (first target))))))

;[["a" 0] ["c" 2]]
;["b" "d"]
;["a" "b" "c" "d"]
(defn position-merge [values target]
  (position-merge-helper (sort-by second values) target []))