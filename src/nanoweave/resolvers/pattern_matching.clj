(ns nanoweave.resolvers.pattern-matching
  (:require [nanoweave.ast.pattern-matching]
            [clojure.string :as str]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.utils :refer [dynamically-load-class contains-many?]])
  (:import [nanoweave.ast.pattern_matching ListPatternMatchOp
            MapPatternMatchOp VariableMatchOp LiteralMatchOp
            KeyMatchOp KeyValueMatchOp Match MatchClause
            RegexMatchOp])
  (:use [nanoweave.ast.base :only [resolve-value Resolvable]]))

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
  ListPatternMatchOp
  (resolve-value [this input]
    (let [bindings (:targets this)]
      (if (and (seqable? bindings) (seqable? input) (not (map? input)))
        (let [binding-count (count bindings)
              input-count (count input)]
          (if (<= (- binding-count input-count) 1)
            (let [[head tail] (split-at (dec binding-count) input)
                  head-bindings (map safe-resolve-value bindings head)
                  tail-binding (safe-resolve-value (last bindings) (vec tail))
                  all-bindings (conj head-bindings tail-binding)]
              (merge-bindings all-bindings))
            {:ok false
             :error (str "Binding pattern [" (str/join ", " bindings) "] has less elements than input (count: " (count input) ")")}))
        {:ok false
         :error (str "Binding pattern [" (str/join ", " bindings) "] can only bind sequences (not maps) but found " (type input))})))
  MapPatternMatchOp
  (resolve-value [this input]
    (let [bindings (safe-resolve-value (:targets this) input)]
      (if (and (seqable? bindings) (map? input))
        (let [mapped-bindings (map #(safe-resolve-value % input) bindings)]
          (merge-bindings mapped-bindings))
        {:ok false
         :error (str "Binding pattern [" (str/join ", " bindings) "] can only bind maps but found " (type input))})))
  VariableMatchOp
  (resolve-value [this input]
    (let [binding-name (safe-resolve-value (:target this) input)]
      {:ok true :bindings {binding-name input}}))
  RegexMatchOp
  (resolve-value [this input]
    (let [pattern (safe-resolve-value (:pattern this) input)
          matches (re-matches pattern input)]
      {:ok (some? matches) :bindings {"_" matches}}))
  KeyValueMatchOp
  (resolve-value [this input]
    (let [key-match (safe-resolve-value (:key this) input)
          [key] (vals (:bindings key-match))
          value-match (safe-resolve-value (:value this) key)
          all-matches [key-match value-match]]
      (merge-bindings all-matches)))
  LiteralMatchOp
  (resolve-value [this input]
    (let [value (safe-resolve-value (:target this) input)
          ok (= value input)]
      {:ok ok :bindings {} :error (if-not ok (str "Literal value [" value "] did not match input [" input "]") nil)}))
  KeyMatchOp
  (resolve-value [this input]
    (let [key (safe-resolve-value (:target this) input)
          value (get input key)]
      {:ok true :bindings {key value}}))
  Match
  (resolve-value [this input]
    (letfn [(find-matching-clause [target clauses]
              (let [clauses-with-match-results
                    (map #(assoc % :match-result (safe-resolve-value (:match %) target)) clauses)
                    clauses-that-match
                    (filter #(-> % :match-result :ok) clauses-with-match-results)]
                (when (empty? clauses-that-match)
                  (throw (AssertionError. (str "Pattern match not exhaustive for input [" target "]"))))
                (first clauses-that-match)))]
      (let [target (safe-resolve-value (:target this) input)
            clauses (:clauses this)
            matching-clause (find-matching-clause target clauses)
            merged-input (merge input (-> matching-clause :match-result :bindings))]
        (safe-resolve-value (:body matching-clause) merged-input)))))
