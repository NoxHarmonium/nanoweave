(ns nanoweave.resolvers.binary-logic
  (:require [nanoweave.ast.binary-logic]
            [nanoweave.resolvers.base :refer [handle-bin-op]]
            [nanoweave.resolvers.operators :refer [xor]]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.binary_logic EqOp NotEqOp LessThanOp LessThanEqOp
            GrThanOp GrThanEqOp AndOp OrOp XorOp]))

(extend-protocol Resolvable
  EqOp
  (resolve-value [this input] (handle-bin-op this input =))
  NotEqOp
  (resolve-value [this input] (handle-bin-op this input not=))
  LessThanOp
  (resolve-value [this input] (handle-bin-op this input <))
  LessThanEqOp
  (resolve-value [this input] (handle-bin-op this input <=))
  GrThanOp
  (resolve-value [this input] (handle-bin-op this input >))
  GrThanEqOp
  (resolve-value [this input] (handle-bin-op this input >=))
  AndOp
  (resolve-value [this input] (handle-bin-op this input #(and %1 %2)))
  OrOp
  (resolve-value [this input] (handle-bin-op this input #(or %1 %2)))
  XorOp
  (resolve-value [this input] (handle-bin-op this input xor)))
