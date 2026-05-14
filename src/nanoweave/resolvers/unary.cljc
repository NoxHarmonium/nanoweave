(ns nanoweave.resolvers.unary
  (:require [nanoweave.ast.unary #?@(:cljs [:refer [NotOp NegOp TypeOfOp]])]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.ast.base :refer [Resolvable]]
            [nanoweave.io-utils :refer [safe-type]])
  #?(:clj (:import [nanoweave.ast.unary NotOp NegOp TypeOfOp])))

(extend-protocol Resolvable
  NotOp
  (resolve-value [this input] (not (safe-resolve-value (:value this) input)))
  NegOp
  (resolve-value [this input] (- (safe-resolve-value (:value this) input)))
  TypeOfOp
  (resolve-value [this input] (safe-type (safe-resolve-value (:value this) input))))
