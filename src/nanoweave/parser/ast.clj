(ns nanoweave.parser.ast
  (:require [schema.core :as s]))

(defprotocol Resolvable
  "Describes an AST node that can be resolved with an input.
  Used to transform AST trees into a final value."
  (resolve-value [this input] "Takes an input and resolves it to a file value."))

(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Str])
(s/defrecord BoolLit [value :- s/Str])
(s/defrecord NilLit [])
(s/defrecord ExprPropAccess [value :- [s/Str]])

(extend-protocol Resolvable
  StringLit
    (resolve-value [this _] (:value this))
  FloatLit
    (resolve-value [this _] (:value this))
  BoolLit
    (resolve-value [this _] (:value this))
  NilLit
    (resolve-value [this _] (:value this))
  ExprPropAccess
    (resolve-value [this input] (reduce #(get %1 %2) input (:value this))))
