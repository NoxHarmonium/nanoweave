(ns nanoweave.resolvers.binary-other
  (:require [nanoweave.ast.binary-other]
            [nanoweave.resolvers.base :refer
             [handle-bin-op handle-prop-access safe-resolve-value]])
  (:import [nanoweave.ast.binary_other DotOp ConcatOp OpenRangeOp ClosedRangeOp])
  (:use [nanoweave.ast.base :only [Resolvable resolve-value]]))

(defn all-sequential? [coll]
  (reduce #(and (sequential? %1) %2) coll))

(extend-protocol Resolvable
  DotOp
  (resolve-value [this input] (handle-prop-access this input))
  ConcatOp
  (resolve-value [this input]
    (let [left (safe-resolve-value (:left this) input)
          right (safe-resolve-value (:right this) input)]
      (if
       (all-sequential? [left right]) ((comp vec concat) left right)
       (str left right))))
  OpenRangeOp
  (resolve-value [this input] (handle-bin-op this input (comp vec range)))
  ClosedRangeOp
  (resolve-value [this input] (handle-bin-op this input (comp vec #(range %1 (+ %2 1))))))
