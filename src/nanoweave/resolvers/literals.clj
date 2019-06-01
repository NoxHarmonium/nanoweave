(ns nanoweave.resolvers.literals
  (:require [nanoweave.ast.literals]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.java-interop :as j])
  (:import [nanoweave.ast.literals IdentiferLit StringLit FloatLit BoolLit NilLit ArrayLit])
  (:use [nanoweave.ast.base :only [resolve-value Resolvable]]))

(extend-protocol Resolvable
  IdentiferLit
  (resolve-value [this input]
    (let [key (:value this)
          resolved-value
          (cond
            (nil? input) input
            (map? input) (get input key)
            (j/matches-reflect-type? input key clojure.reflect.Method) (j/wrap-java-fn input key)
            (j/matches-reflect-type? input key clojure.reflect.Field) (j/get-java-field input key)
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
    (into [] (map #(safe-resolve-value %1 input) (:value this)))))
