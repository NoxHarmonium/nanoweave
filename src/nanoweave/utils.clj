(ns nanoweave.utils
  (:require [clojure.walk :refer [prewalk]]
            [clojure.data.json :as json]))

(defn read-json-with-doubles [string]
  "Reads JSON but makes sure that numbers are read as doubles.
   In nanoweave we currently deal with all numbers as doubles,
   like in Javascript. This may change in the future but keeps
   it simple for now."
  (let [json-map (json/read-str string)]
    (prewalk #(if (number? %1) (double %1) %1) json-map)))

; From https://github.com/Prismatic/plumbing/
(defn map-vals
  "Build map k -> (f v) for [k v] in map, preserving the initial type"
  [f m]
  (cond
    (sorted? m)
    (reduce-kv (fn [out-m k v] (assoc out-m k (f v))) (sorted-map) m)
    (map? m)
    (persistent! (reduce-kv (fn [out-m k v] (assoc! out-m k (f v))) (transient {}) m))
    :else nil))

(defn dynamically-load-class
  "Loads a Java class into the current namespace and returns it"
  [class-name]
  (.importClass (the-ns *ns*)
                (clojure.lang.RT/classForName class-name))
  (clojure.lang.RT/classForName class-name))