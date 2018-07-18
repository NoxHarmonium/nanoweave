(ns jweave.parser.ast
  (:require [schema.core :as s
             ;:include-macros true ;; cljs only
             ]))

;(defn read-value [key obj] (. key obj))

(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Str])
(s/defrecord BoolLit [value :- s/Str])
(s/defrecord NilLit [])
(s/defrecord ExprPropAccess [value :- [s/Str]])
