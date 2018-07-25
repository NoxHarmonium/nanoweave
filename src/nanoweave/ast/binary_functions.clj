(ns nanoweave.ast.binary-functions
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord MapOp [left :- Resolvable right :- Resolvable])
(s/defrecord FilterOp [left :- Resolvable right :- Resolvable])
(s/defrecord ReduceOp [left :- Resolvable right :- Resolvable])

(extend-protocol Resolvable
  MapOp
  (resolve-value [this input] (handle-bin-op this input #(map %2 %1))))

(extend-protocol Resolvable
  FilterOp
  (resolve-value [this input] (handle-bin-op this input #(filter %2 %1))))

; Future work: What should reduce return on an empty collection with no default value?
; Clojure specifies that the reducer function must accept no args and return a sensible default value
; but that isn't really practical here. Even Haskell throws an exception in this case.
(extend-protocol Resolvable
  ReduceOp
  (resolve-value [this input] (handle-bin-op this input #(if (empty? %1) nil (reduce %2 %1)))))