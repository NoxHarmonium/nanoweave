(ns nanoweave.ast.literals
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord IdentiferLit [value :- s/Str])
(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Num])
(s/defrecord BoolLit [value :- s/Bool])
(s/defrecord NilLit [])
(s/defrecord ArrayLit [value :- [Resolvable]])


(extend-protocol Resolvable
  IdentiferLit
  (resolve-value [this input]
    (get input (:value this)))
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
    (map #(safe-resolve-value %1 input) (:value this))))