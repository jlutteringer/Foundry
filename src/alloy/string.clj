(ns alloy.string
  (:require [alloy.util :as util]))

(def default-character-encoding :utf-8)

(defn shrink [s] (clojure.string/replace s " " ""))
(defn note [& args] (clojure.string/join " " args))

(defn capitalize-all
  "Capitalize every word in a string"
  [s]
  (->> (clojure.string/split (str s) #"\b")
       (map clojure.string/capitalize)
       (clojure.string/join)))

(defn re-contains [pattern s]
  (->> s (re-find pattern) nil? not))
(def has-space #(re-contains #" " %))
(def has-underscore #(re-contains #"_" %))
(def has-capital #(re-contains #"[A-Z]" %))
(def has-dash #(re-contains #"\-" %))
(def is-uppercase #(= (clojure.string/upper-case %) %))
(def is-lowercase #(= (clojure.string/lower-case %) %))
(def is-capitalized #(re-contains #"^[A-Z]" %))

(defn- tokenizer-factory
  ([splitter]
   (tokenizer-factory splitter (partial map clojure.string/lower-case)))
  ([splitter formatter]
   #(if (util/pattern? splitter)
     (-> % (clojure.string/split splitter) formatter)
     (-> % splitter formatter))))

(defn- untokenizer-factory
  ([formatter] (untokenizer-factory "" formatter))
  ([joiner formatter]
    (let [joiner (if (string? joiner) #(clojure.string/join joiner %) joiner)]
      #(->> %
        (map-indexed formatter)
        joiner))))

(def formatters {
  :pretty {
    :tokenize (tokenizer-factory #" ")
    :untokenize (untokenizer-factory " " (util/fn-rest clojure.string/capitalize))
    :match has-space
  }
  :underscore-uppercase {
    :tokenize (tokenizer-factory #"_")
    :untokenize (untokenizer-factory "_" (util/fn-rest clojure.string/upper-case))
    :match (every-pred has-underscore is-uppercase)
  }
  :underscore-lowercase {
    :tokenize (tokenizer-factory #"_" identity)
    :untokenize (untokenizer-factory "_" (util/fn-rest identity))
    :match (every-pred has-underscore is-lowercase)
  }
  :dash-uppercase {
    :tokenize (tokenizer-factory #"\\-")
    :untokenize (untokenizer-factory "-" (util/fn-rest clojure.string/upper-case))
    :match (every-pred has-dash is-lowercase)
  }
  :dash-lowercase {
    :tokenize (tokenizer-factory #"\\-" identity)
    :untokenize (untokenizer-factory "-" (util/fn-rest identity))
    :match (every-pred has-dash is-lowercase)
  }
  :camel-uppercase {
    :tokenize (tokenizer-factory  #"(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])")
    :untokenize (untokenizer-factory (util/fn-rest clojure.string/capitalize))
    :match (every-pred is-capitalized (complement has-space) (complement has-underscore))
  }
  :camel-lowercase {
    :tokenize (tokenizer-factory  #"(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])")
    :untokenize (untokenizer-factory (fn [i s] (if (zero? i) s (clojure.string/capitalize s))))
    :match (every-pred (complement has-space) (complement has-underscore))
  }
})

(defn gather-formats [] (-> formatters keys set))
(defn gather-scanners [] (map list (->> formatters keys) (->> formatters vals (map :match))))

(defn tokenize [s format] ((-> formatters format :tokenize) s))
(defn untokenize [tokens format] ((-> formatters format :untokenize) tokens))

(defn detect-format [s]
  (first (util/find-first #((second %) s) (gather-scanners))))

(defn convert-format
  ([s target-format]
   (convert-format s (detect-format (clojure.string/trim s)) target-format))
  ([s current-format target-format]
    {:pre [((gather-formats) current-format) ((gather-formats) target-format)]}
    (-> s (clojure.string/trim) (tokenize current-format) (untokenize target-format))))