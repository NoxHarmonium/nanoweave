(ns nanoweave.utils
  (:require [clojure.walk :refer [prewalk]]
            [cheshire.core :as cc]))

(defn read-json-with-doubles
  "Reads JSON but makes sure that numbers are read as doubles.
   In nanoweave we currently deal with all numbers as doubles,
   like in Javascript. This may change in the future but keeps
   it simple for now."
  [string]
  (let [json-map (cc/parse-string string)]
    (prewalk #(if (number? %1) (double %1) %1) json-map)))

(defn convert-to-number
  "You can coerce strings or other number types in to a Nanoweave Number type/
   This makes sure that the conversion is done in a 'smart' way.
   It will try very hard to do the conversion, even converting it to a string first,
   but it will still blow up if the input doesn't make sense"
  [anything]
  (cond
    (number? anything) (double anything)
    (string? anything) (Double/parseDouble anything)
    (boolean? anything) (if anything 1.0 0.0)
    (nil? anything) anything
    :else (try
            (Double/parseDouble (str anything))
            (catch Exception _ (str "Cannot convert type '" (type anything) "' to a number.")))))

(defn safe-type
  "Does the same thing as the built-in Clojure type function,
   but will return 'Nil' instead of an empty string if it is passed
   a nil value"
  [anything]
  (if (nil? anything) "Nil" (str (type anything))))

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

; Thanks: https://stackoverflow.com/a/27914262

(defn contains-many?
  "Checks if a map m contains all the keys in sequence ks"
  [m ks]
  (every? #(contains? m %) ks))

; Thanks: https://stackoverflow.com/a/20054111

(defmacro declare-extern
  [& syms]
  (let [n (ns-name *ns*)]
    `(do
       ~@(for [s syms]
           `(do
              (ns ~(symbol (namespace s)))
              (declare ~(symbol (name s)))))
       (in-ns '~n))))

; Thanks: https://stackoverflow.com/a/4831170/1153203

(defn find-thing [needle haystack]
  (keep-indexed #(when (= %2 needle) %1) haystack))
