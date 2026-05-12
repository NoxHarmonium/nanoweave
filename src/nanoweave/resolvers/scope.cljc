(ns nanoweave.resolvers.scope
  (:require [nanoweave.ast.scope #?@(:cljs [:refer [Binding Expression ImportOp Indexing When]])]
            [nanoweave.ast.base :refer [Resolvable]]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.resolvers.errors :refer [throw-resolve-error]]
            #?(:clj [nanoweave.utils :refer [dynamically-load-class]]))
  #?(:clj (:import [nanoweave.ast.scope Binding Expression ImportOp Indexing When])))

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
    (let [class-name (:class-name this)]
      #?(:clj (let [class (dynamically-load-class class-name)] class)
         :cljs (do (js/console.warn "Java import not supported in ClojureScript:" class-name) nil))))
  When
  (resolve-value [this input]
    (letfn [(find-matching-clause [clauses]
              (let [clauses-that-match
                    (filter #(safe-resolve-value (:condition %) input) clauses)]
                (when (empty? clauses-that-match)
                  (throw-resolve-error (str "Pattern match not exhaustive for input [" input "]") this))
                (first clauses-that-match)))]
      (let [clauses (:clauses this)
            matching-clause (find-matching-clause clauses)]
        (safe-resolve-value (:body matching-clause) input)))))
