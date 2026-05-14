(ns nanoweave.resolvers.text
  (:require [nanoweave.ast.text #?@(:cljs [:refer [InterpolatedString Regex]])]
            [clojure.string :as str]
            [nanoweave.ast.base :refer [Resolvable]]
            [nanoweave.resolvers.base :refer [safe-resolve-value]])
  #?(:clj (:import [nanoweave.ast.text InterpolatedString Regex])))

(extend-protocol Resolvable
  InterpolatedString
  (resolve-value [this input]
    (let [elements (:body this)]
      (str/join
       (map #(safe-resolve-value % input) elements))))
  Regex
  (resolve-value [this _]
    (:regex this)))
