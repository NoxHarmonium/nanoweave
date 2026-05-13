(ns nanoweave.resolvers.literals
  (:require [nanoweave.ast.literals :refer [IdentiferLit StringLit FloatLit BoolLit NilLit ArrayLit TypeLit PairLit ObjectLit]]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.resolvers.errors :refer [throw-resolve-error]]
            [clojure.string :as string]
            [nanoweave.ast.base :refer [Resolvable]]))

(extend-protocol Resolvable
  IdentiferLit
  (resolve-value [this input]
    (let [key (:value this)]
      (cond
        (nil? input) input
        (map? input) (get input key)
        :else (throw-resolve-error (str "Not sure how to resolve key [" key "] on [" (type input) "]") this))))
  StringLit
  (resolve-value [this _] (:value this))
  FloatLit
  (resolve-value [this _] (:value this))
  BoolLit
  (resolve-value [this _] (:value this))
  NilLit
  (resolve-value [_ _] nil)
  ArrayLit
  (resolve-value [this input]
    (into [] (map #(safe-resolve-value %1 input) (:value this))))
  TypeLit
  (resolve-value [this _] (keyword (string/lower-case (:value this))))
  PairLit
  (resolve-value [this input]
    (let [{:keys [key value]} this]
      [(safe-resolve-value key input)
       (safe-resolve-value value input)]))
  ObjectLit
  (resolve-value [this input]
    (let [pairs (:pairs this)
          resolved-pairs (map #(safe-resolve-value % input) pairs)]
      (apply hash-map (reduce concat [] resolved-pairs)))))
