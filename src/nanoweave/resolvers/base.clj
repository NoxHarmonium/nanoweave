(ns ^{:doc "Provides functions to walk the AST parsed by the parsing functions
            and transform values.", :author "Sean Dawson"}
 nanoweave.resolvers.base
  (:require [nanoweave.ast.base :refer [resolve-value Resolvable]]))

(defn safe-resolve-value
  "Resolves the input if the given resolver is not nil and conforms to Resolvable,
  otherwise it will just return the input."
  [resolver input]
  (when (some? resolver)
    (if (satisfies? Resolvable resolver)
      (resolve-value resolver input)
      (println "Warning: Resolving unknown type: [" (type input) "] Will return nil"))))

(defn handle-prop-access
  "A special case binary resolver.
  If the left side is an identifier then there is no existing value to access
  so it will get the value from scope. Otherwise it will just use the right
  side as a key to access the left side."
  [this input]
  (let [left (safe-resolve-value (:left this) input)]
    (safe-resolve-value (:right this) left)))

(defn handle-bin-op
  "Binary resolver for generic binary operations."
  [this input op]
  (op (safe-resolve-value (:left this) input)
      (safe-resolve-value (:right this) input)))
