(ns nanoweave.ast.scope
  (:require [schema.core :as s]
            [clojure.string :as str]
            [nanoweave.utils :refer [dynamically-load-class contains-many?]])
  (:use nanoweave.ast.base))

(s/defrecord Binding [match :- Resolvable value :- Resolvable body :- Resolvable])
(s/defrecord Expression [body :- Resolvable])
(s/defrecord InterpolatedString [body :- [Resolvable]])
(s/defrecord Indexing [target :- Resolvable key :- Resolvable])
(s/defrecord ImportOp [class-name :- Resolvable])
(s/defrecord ListPatternMatchOp [targets :- [Resolvable]])
(s/defrecord MapPatternMatchOp [targets :- [Resolvable]])
(s/defrecord VariableMatchOp [target :- Resolvable])

(extend-protocol Resolvable
  Binding
  (resolve-value [this input]
    (let [body (:body this)
          value (safe-resolve-value (:value this) input)
          associated-values (safe-resolve-value (:match this) value)
          merged-input (merge input associated-values)]
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
          key (first (safe-resolve-value (:key this) input))]
      (cond
        (map? target) (target key)
        (string? target) (str (nth target key))
        (seqable? target) (nth target key)
        :else nil)))
  ImportOp
  (resolve-value [this _]
    (let [class-name (:class-name this)
          class (dynamically-load-class class-name)]
      class))
  ListPatternMatchOp
  (resolve-value [this input]
    (let [binding-names (safe-resolve-value (:targets this) input)]
      (if (and (seqable? binding-names) (vector? input))
        (let [binding-count (count binding-names)
              input-count (count input)]
          (if (= binding-count input-count)
            (zipmap binding-names input)
            (throw (Exception. (str "Binding pattern [" (str/join ", " binding-names) "] does not match size of input " (str/join ", " input))))))
        (throw (Exception. (str "Binding pattern [" (str/join ", " binding-names) "] can only bind arrays but found " (type input)))))))
  MapPatternMatchOp
  (resolve-value [this input]
    (let [binding-names (safe-resolve-value (:targets this) input)]
      (if (and (seqable? binding-names) (map? input))
        (if (contains-many? input binding-names)
          (into {} (map #(assoc {} % (get input %)) binding-names))
          (throw (Exception. (str "Binding pattern [" (str/join ", " binding-names) "] does not match size of input " (str/join ", " input)))))
        (throw (Exception. (str "Binding pattern [" (str/join ", " binding-names) "] can only bind maps but found " (type input)))))))
  VariableMatchOp
  (resolve-value [this input]
    (let [binding-name (safe-resolve-value (:target this) input)]
      {binding-name input})))
