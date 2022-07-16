(ns nanoweave.resolvers.binary-other
  (:require [nanoweave.ast.binary-other]
            [nanoweave.resolvers.base :refer
             [handle-bin-op handle-prop-access safe-resolve-value]]
            [nanoweave.utils :refer [dynamically-load-class]])
  (:import [nanoweave.ast.binary_other DotOp ConcatOp OpenRangeOp ClosedRangeOp IsOp])
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
  (resolve-value [this input] (handle-bin-op this input (comp vec #(range %1 (inc %2)))))
  IsOp
  (resolve-value [this input]
    (handle-bin-op this input
                   #(case %2
                      :number (number? %1)
                      :string  (string? %1)
                      :boolean  (boolean? %1)
                      :nil   (nil? %1)
                      ; TODO: Should we check for seqable??
                      :array (vector? %1)
                      (if (string? %2) (instance? (dynamically-load-class %2) %1)
                          (throw (AssertionError. (str "Unknown type '" (type %2) "' for type checking. Should either be one of the type literals Number, String, Boolean, Nil or Array or a string referring to a fully qualified Java class"))))))))