(ns nanoweave.resolvers.scope
  (:require [nanoweave.ast.scope]
            [nanoweave.ast.base :refer :all]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.utils :refer [dynamically-load-class]])
  (:import [nanoweave.ast.scope Binding Expression ImportOp Indexing When]))

(defn- merge-bindings
  "Merges the results of multiple pattern matches.
   If one match fails, the entire match fails."
  [bindings]
  (let [bindings-with-errors (remove :ok bindings)
        ok (empty? bindings-with-errors)
        merged-bindings (when ok (into {} (map :bindings bindings)))]
    {:ok ok
     :bindings merged-bindings
     :error (:error (first bindings-with-errors))}))

(extend-protocol Resolvable
  Binding
  (resolve-value [this input]
    (let [body (:body this)
          value (safe-resolve-value (:value this) input)
          associated-values (:bindings (safe-resolve-value (:match this) value))
          merged-input (merge input associated-values)]
      (safe-resolve-value body merged-input)))
  Expression
  (resolve-value [this input]
    (safe-resolve-value (:body this) input))
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
  When
  (resolve-value [this input]
    (letfn [(find-matching-clause [clauses]
              (let [clauses-that-match
                    (filter #(safe-resolve-value (:condition %) input) clauses)]
                (when (empty? clauses-that-match)
                  (throw (AssertionError. (str "Pattern match not exhaustive for input [" input "]"))))
                (first clauses-that-match)))]
      (let [clauses (:clauses this)
            matching-clause (find-matching-clause clauses)]
        (safe-resolve-value (:body matching-clause) input)))))
