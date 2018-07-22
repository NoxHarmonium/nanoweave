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