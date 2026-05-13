(ns nanoweave.resolvers.binary-other
  (:require [nanoweave.ast.binary-other :refer [DotOp ConcatOp OpenRangeOp ClosedRangeOp IsOp AsOp]]
            [nanoweave.resolvers.base :refer [handle-bin-op handle-prop-access safe-resolve-value]]
            [nanoweave.utils :refer [convert-to-number]]
            [nanoweave.ast.base :refer [Resolvable]]))

(defn- all-sequential? [coll]
  (reduce #(and (sequential? %1) %2) coll))

(extend-protocol Resolvable
  DotOp
  (resolve-value [this input] (handle-prop-access this input))
  ConcatOp
  (resolve-value [this input]
    (let [left (safe-resolve-value (:left this) input)
          right (safe-resolve-value (:right this) input)]
      (if (all-sequential? [left right])
        ((comp vec concat) left right)
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
                      :string (string? %1)
                      :boolean (boolean? %1)
                      :nil (nil? %1)
                      :array (vector? %1)
                      (do
                        (js/console.warn "Java class type checking not supported in ClojureScript:" %2)
                        false))))
  AsOp
  (resolve-value [this input]
    (handle-bin-op this input
                   #(case %2
                      :number (convert-to-number %1)
                      :string (str %1)
                      :boolean (boolean %1)
                      :nil nil
                      :array (vec %1)
                      (do
                        (js/console.warn "Java class type coercion not supported in ClojureScript:" %2)
                        nil)))))
