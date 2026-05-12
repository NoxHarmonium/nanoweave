(ns nanoweave.demo.examples
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- fixture-resource [folder file]
  (io/resource (str "test-fixtures/" folder "/" file)))

(defn- read-fixture [folder file]
  (when-let [r (fixture-resource folder file)]
    (slurp r)))

(defn- folder->label [folder]
  (-> folder
      (str/replace #"-" " ")
      (str/split #" ")
      (->> (map str/capitalize)
           (str/join " "))))

(defmacro load-examples []
  (let [folders ["simple-structure-transform"
                 "basic-arithmetic"
                 "basic-variables"
                 "boolean-logic"
                 "concat-operator"
                 "string-interpolation"
                 "basic-functional-operators"
                 "function-calling"
                 "indexing"
                 "ranges"
                 "map-collection"
                 "pattern-matching"
                 "flow-control"
                 "regex"]]
    (mapv (fn [folder]
            {:label (folder->label folder)
             :input (read-fixture folder "input.json")
             :transform (read-fixture folder "transform.nweave")})
          folders)))
