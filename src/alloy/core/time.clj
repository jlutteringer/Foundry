(ns alloy.core.time
  (:require [clj-time.core :as time]
            [alloy.core.core :refer :all]))

(record Duration [amount [unit :millisecond]])

;TODO actually implement this thing
(defn amount [duration unit]
  (:amount duration))