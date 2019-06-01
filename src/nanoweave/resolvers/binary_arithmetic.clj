(ns nanoweave.resolvers.binary-arithmetic
  (:require [nanoweave.ast.binary-arithmetic]
            [nanoweave.resolvers.base :refer [handle-bin-op]])
  (:import [nanoweave.ast.binary_arithmetic AddOp SubOp MultOp DivOp ModOp])
  (:use [nanoweave.ast.base :only [resolve-value Resolvable]]))

(extend-protocol Resolvable
  AddOp
  (resolve-value [this input] (handle-bin-op this input +))
  SubOp
  (resolve-value [this input] (handle-bin-op this input -))
  MultOp
  (resolve-value [this input] (handle-bin-op this input *))
  DivOp
  (resolve-value [this input] (handle-bin-op this input /))
  ModOp
  (resolve-value [this input] (handle-bin-op this input mod)))
