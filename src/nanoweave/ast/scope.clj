(ns nanoweave.ast.scope
  (:require [schema.core :as s]
            [nanoweave.utils :refer [dynamically-load-class]])
  (:use nanoweave.ast.base))

(s/defrecord Binding [target :- Resolvable value :- Resolvable body :- Resolvable])
(s/defrecord Expression [body :- Resolvable])
(s/defrecord InterpolatedString [body :- [Resolvable]])
(s/defrecord Indexing [target :- Resolvable key :- Resolvable])
(s/defrecord ImportOp [class-name :- Resolvable])

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
             (map #(safe-resolve-value % input) elements))))
  Indexing
  (resolve-value [this input]
    (let [target (safe-resolve-value (:target this) input)
          key (safe-resolve-value (:key this) input)]
      (cond
        (map? target) (target key)
        (string? target) (str (nth target key))
        (seqable? target) (nth target key)
        :else nil)))
  ImportOp
  (resolve-value [this _]
    (let [class-name (:class-name this)
          class (dynamically-load-class class-name)]
      class)))