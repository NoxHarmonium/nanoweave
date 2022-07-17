(ns nanoweave.resolvers.unary
  (:require [nanoweave.ast.unary]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.unary NotOp NegOp]))

(extend-protocol Resolvable
  NotOp
  (resolve-value [this input] (not (safe-resolve-value (:value this) input)))
  NegOp
  (resolve-value [this input] (- (safe-resolve-value (:value this) input))))

