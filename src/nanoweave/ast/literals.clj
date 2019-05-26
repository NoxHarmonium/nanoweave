(ns nanoweave.ast.literals
  (:require [schema.core :as s]
            [clojure.reflect :as r]
            [nanoweave.ast.base :refer [Resolvable safe-resolve-value]]
            [nanoweave.java-interop :as j]))

(s/defrecord IdentiferLit [value :- s/Str])
(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Num])
(s/defrecord BoolLit [value :- s/Bool])
(s/defrecord NilLit [])
(s/defrecord ArrayLit [value :- [Resolvable]])

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
