(ns nanoweave.ast.unary
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable safe-resolve-value]]))

(s/defrecord NotOp [value :- Resolvable])
(s/defrecord NegOp [value :- Resolvable])

(extend-protocol Resolvable
  NotOp
  (resolve-value [this input] (not (safe-resolve-value (:value this) input)))
  NegOp
  (resolve-value [this input] (- (safe-resolve-value (:value this) input))))

