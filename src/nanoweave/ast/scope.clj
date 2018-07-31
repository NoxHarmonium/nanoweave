(ns nanoweave.ast.scope
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord Binding [target :- Resolvable value :- Resolvable body :- Resolvable])
(s/defrecord Expression [body :- Resolvable])
(s/defrecord InterpolatedString [body :- [Resolvable]])

(extend-protocol Resolvable
  Binding
  (resolve-value [this input]
    (let [body (:body this)
          target (safe-resolve-value (:target this) input)
          value (safe-resolve-value (:value this) input)
          merged-input (merge input {target value})]
      (safe-resolve-value body merged-input)))
  Expression
  (resolve-value [this input]
    (safe-resolve-value (:body this) input))
  InterpolatedString
  (resolve-value [this input]
    (let [elements (:body this)]
      (apply str
             (map #(safe-resolve-value % input) elements)))))
