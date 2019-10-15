(ns nanoweave.resolvers.text
  (:require [nanoweave.ast.text]
            [clojure.string :as str]
            [nanoweave.ast.base :refer :all]
            [nanoweave.resolvers.base :refer [safe-resolve-value]])
  (:import [nanoweave.ast.text InterpolatedString Regex]))

(extend-protocol Resolvable
  InterpolatedString
  (resolve-value [this input]
    (let [elements (:body this)]
      (str/join
       (map #(safe-resolve-value % input) elements))))
  Regex
  (resolve-value [this input]
    (:regex this)))
