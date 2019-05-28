(ns nanoweave.ast.scope
  (:require [schema.core :as s]
            [clojure.string :as str]
            [nanoweave.ast.base :refer [Resolvable safe-resolve-value]]
            [nanoweave.utils :refer [dynamically-load-class contains-many?]]))

(s/defrecord Binding [match :- Resolvable value :- Resolvable body :- Resolvable])
(s/defrecord Expression [body :- Resolvable])
(s/defrecord InterpolatedString [body :- [Resolvable]])
(s/defrecord Indexing [target :- Resolvable key :- Resolvable])
(s/defrecord ImportOp [class-name :- Resolvable])
(s/defrecord ListPatternMatchOp [targets :- [Resolvable]])
(s/defrecord MapPatternMatchOp [targets :- [Resolvable]])
(s/defrecord VariableMatchOp [target :- Resolvable])
(s/defrecord LiteralMatchOp [target :- Resolvable])
(s/defrecord When [clauses :- [Resolvable]])
(s/defrecord WhenClause [condition :- Resolvable body :- Resolvable])
(s/defrecord Match [clauses :- [Resolvable] target :- Resolvable])
(s/defrecord MatchClause [match :- Resolvable body :- Resolvable])

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
    (let [bindings (:targets this)]
      (if (and (seqable? bindings) (seqable? input))
        (let [binding-count (count bindings)
              input-count (count input)]
          (if (<= (- binding-count input-count) 1)
            (let [[head tail] (split-at (- binding-count 1) input)
                  head-bindings (map #(safe-resolve-value %1 %2) bindings head)
                  tail-binding (safe-resolve-value (last bindings) (vec tail))
                  all-bindings (conj head-bindings tail-binding)
                  errors (remove :ok all-bindings)
                  ok (empty? errors)
                  merged-bindings (if ok (into {} (map :bindings all-bindings)) nil)]
              {:ok ok
               :bindings merged-bindings
               :error (first errors)})
            {:ok false
             :error (str "Binding pattern [" (str/join ", " bindings) "] has less elements than input (count: " (count input) ")")}))
        {:ok false
         :error (str "Binding pattern [" (str/join ", " bindings) "] can only bind arrays but found " (type input))})))
  MapPatternMatchOp
  (resolve-value [this input]
    (let [binding-names (safe-resolve-value (:targets this) input)]
      (if (and (seqable? binding-names) (map? input))
        (if (contains-many? input binding-names)
          {:ok true
           :bindings (into {} (map #(assoc {} % (get input %)) binding-names))}
          {:ok false
           :error (str "Binding pattern [" (str/join ", " binding-names) "] does not match size of input " (str/join ", " input))})
        {:ok false
         :error (str "Binding pattern [" (str/join ", " binding-names) "] can only bind maps but found " (type input))})))
  VariableMatchOp
  (resolve-value [this input]
    (let [binding-name (safe-resolve-value (:target this) input)]
      {:ok true :bindings {binding-name input}}))
  LiteralMatchOp
  (resolve-value [this input]
    (let [value (safe-resolve-value (:target this) input)]
      {:ok (= value input) :bindings {}}))
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
        (safe-resolve-value (:body matching-clause) input))))
  Match
  (resolve-value [this input]
    (letfn [(find-matching-clause [target clauses]
              (let [clauses-with-match-results
                    (map #(assoc % :match-result (safe-resolve-value (:match %) target)) clauses)
                    clauses-that-match
                    (filter #(-> % :match-result :ok) clauses-with-match-results)]
                (when (empty? clauses-that-match)
                  (println "Match candidates: " (map #(-> % :match-result) clauses-with-match-results))
                  (throw (AssertionError. (str "Pattern match not exhaustive for input [" target "]"))))
                (first clauses-that-match)))]
      (let [target (safe-resolve-value (:target this) input)
            clauses (:clauses this)
            matching-clause (find-matching-clause target clauses)
            merged-input (merge input (-> matching-clause :match-result :bindings))]
        (safe-resolve-value (:body matching-clause) merged-input)))))

