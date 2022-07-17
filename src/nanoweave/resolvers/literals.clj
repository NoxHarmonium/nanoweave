(ns nanoweave.resolvers.literals
  (:require [nanoweave.ast.literals]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.java-interop :as j]
            [clojure.string :as string]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.literals IdentiferLit StringLit FloatLit BoolLit NilLit ArrayLit TypeLit]))

(extend-protocol Resolvable
  IdentiferLit
  (resolve-value [this input]
    (let [key (:value this)
          is-static (:static-prefix this)
          resolved-value
          (cond
            (nil? input) input
            (map? input) (get input key)
            (j/matches-reflect-type? input key clojure.reflect.Method is-static) (j/wrap-java-fn input key is-static)
            (j/matches-reflect-type? input key clojure.reflect.Field is-static) (j/get-java-field input key is-static)
            :else (throw (Exception. (str "Not sure how to resolve key [" key "] on [" (type input) "]"))))]
      resolved-value))
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
  (resolve-value [this _] (keyword (string/lower-case (:value this)))))
