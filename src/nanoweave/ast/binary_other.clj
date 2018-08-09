(ns nanoweave.ast.binary-other
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord DotOp [left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [left :- Resolvable right :- Resolvable])
(s/defrecord OpenRangeOp [left :- Resolvable right :- Resolvable])
(s/defrecord ClosedRangeOp [left :- Resolvable right :- Resolvable])

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
