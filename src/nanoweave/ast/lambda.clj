(ns nanoweave.ast.lambda
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord Lambda [param-list :- [s/Str] body :- Resolvable])

(extend-protocol Resolvable
  Lambda
  (resolve-value [this input]
    (let [param-list (:param-list this)
          body (:body this)]
      (fn [& args]
        (assert (= (count args) (count param-list)) "incorrect number of params passed to lambda")
        (safe-resolve-value body (merge
                                   input
                                   (zipmap param-list args)))))))
