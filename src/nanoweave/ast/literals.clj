(ns ^{:doc "Syntax that represents literal values."
      :author "Sean Dawson"}
 nanoweave.ast.literals
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord IdentiferLit [value :- s/Str static-prefix :- s/Bool])
(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Num])
(s/defrecord BoolLit [value :- s/Bool])
(s/defrecord NilLit [])
(s/defrecord ArrayLit [value :- [Resolvable]])
(s/defrecord TypeLit [value :- (s/enum "Number" "String" "Boolean" "Nil" "Array")])
