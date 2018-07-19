(ns jweave.parser.ast
  (:require [schema.core :as s
             ;:include-macros true ;; cljs only
             ]))

;(defn read-value [key obj] (. key obj))

(defprotocol Resolvable
  "Describes an AST node that can be resolved with an input.
  Used to transform AST trees into a final value."
  (resolve [this input] "Takes an input and resolves it to a file value."))

(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Str])
(s/defrecord BoolLit [value :- s/Str])
(s/defrecord NilLit [])
(s/defrecord ExprPropAccess [value :- [s/Str]])

(extend-protocol Resolvable
  StringLit
  (resolve [this _] (:value this))
  FloatLit
  (resolve [this _] (:value this))
  BoolLit
  (resolve [this _] (:value this))
  NilLit
  (resolve [this _] (:value this))
  ExprPropAccess
  (resolve [this input] (reduce #(get %1 %2) input (:value this))))
  ;(resolve [this input] (reduce #(. %1 %2) input (:value this))))