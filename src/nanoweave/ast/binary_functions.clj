(ns nanoweave.ast.binary-functions
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable handle-bin-op]]))

(s/defrecord MapOp [left :- Resolvable right :- Resolvable])
(s/defrecord FilterOp [left :- Resolvable right :- Resolvable])
(s/defrecord ReduceOp [left :- Resolvable right :- Resolvable])

(defn wrap-lambda
  "Returns a function that wraps the given lambda so that it will be
   called with the correct signature even if passed to a standard Clojure function"
  [input lambda]
  (fn [& args] (apply lambda (cons input args))))

(extend-protocol Resolvable
  MapOp
  (resolve-value [this input]
    (handle-bin-op this input #(map (wrap-lambda input %2) %1))))

(extend-protocol Resolvable
  FilterOp
  (resolve-value [this input]
    (handle-bin-op this input #(filter (wrap-lambda input %2) %1))))

; Future work: What should reduce return on an empty collection with no default value?
; Clojure specifies that the reducer function must accept no args and return a sensible default value
; but that isn't really practical here. Even Haskell throws an exception in this case.
(extend-protocol Resolvable
  ReduceOp
  (resolve-value [this input] (handle-bin-op this input #(if (empty? %1) nil (reduce (wrap-lambda input %2) %1)))))
