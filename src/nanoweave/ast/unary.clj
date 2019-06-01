(ns nanoweave.ast.unary
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord NotOp [value :- Resolvable])
(s/defrecord NegOp [value :- Resolvable])
