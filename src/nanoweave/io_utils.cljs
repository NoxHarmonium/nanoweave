(ns nanoweave.io-utils
  (:require [clojure.walk :refer [prewalk]]))

(defn read-json-with-doubles
  "Reads JSON but makes sure that numbers are read as doubles.
  In nanoweave we currently deal with all numbers as doubles,
  like in Javascript."
  [string]
  (let [json-map (js->clj (js/JSON.parse string))]
    (prewalk #(if (number? %1) (double %1) %1) json-map)))

(defn convert-to-number
  "Coerces strings or other number types into a Nanoweave Number type."
  [anything]
  (cond
    (number? anything) (double anything)
    (string? anything) (js/parseFloat anything)
    (boolean? anything) (if anything 1.0 0.0)
    (nil? anything) anything
    :else (let [n (js/parseFloat (str anything))]
            (if (js/isNaN n)
              (str "Cannot convert type '" (type anything) "' to a number.")
              n))))

(defn safe-type
  "Returns the type of a value as a string.
  Uses the JS constructor name (e.g. \"Number\") when available,
  falling back to (str (type x)) for types without a named constructor."
  [anything]
  (if (nil? anything)
    "null"
    (let [n (.-name (type anything))]
      (if (seq n) n (str (type anything))))))

(defn dynamically-load-class
  "Not supported in ClojureScript."
  [class-name]
  (js/console.warn "dynamically-load-class not supported in ClojureScript:" class-name)
  nil)
