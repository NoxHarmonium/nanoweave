(ns nanoweave.resolvers.binary-functions
  (:require [nanoweave.ast.binary-functions]
            [nanoweave.resolvers.base :refer [handle-bin-op]]
            [clojure.string :as str]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.binary_functions MapOp FilterOp ReduceOp RegexMatchOp RegexFindOp RegexSplitOp]))

(defn wrap-lambda
  "Returns a function that wraps the given lambda so that it will be
   called with the correct signature even if passed to a standard Clojure function"
  [input lambda]
  (fn [& args] (apply lambda (cons input args))))

(extend-protocol Resolvable
  MapOp
  (resolve-value [this input]
    (handle-bin-op this input #(map (wrap-lambda input %2) %1)))
  FilterOp
  (resolve-value [this input]
    (handle-bin-op this input #(filter (wrap-lambda input %2) %1)))
  ; Future work: What should reduce return on an empty collection with no default value?
  ; Clojure specifies that the reducer function must accept no args and return a sensible default value
  ; but that isn't really practical here. Even Haskell throws an exception in this case.
  ReduceOp
  (resolve-value [this input] (handle-bin-op this input
                                             #(if (empty? %1) nil (reduce (wrap-lambda input %2) %1))))
  RegexMatchOp
  (resolve-value [this input] (handle-bin-op this input #(re-matches %2 %1)))
  RegexFindOp
  (resolve-value [this input] (handle-bin-op this input #(re-find %2 %1)))
  RegexSplitOp
  (resolve-value [this input] (handle-bin-op this input #(str/split %1 %2))))
