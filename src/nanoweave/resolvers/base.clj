(ns ^{:doc "Provides functions to walk the AST parsed by the parsing functions
            and transform values.", :author "Sean Dawson"}
 nanoweave.resolvers.base
  (:require [nanoweave.ast.base :refer [Resolvable resolve-value]]
            [nanoweave.resolvers.errors :refer [throw-resolve-error resolve-error?]]))

(defn safe-resolve-value
  "Resolves the input if the given resolver is not nil and conforms to Resolvable,
  otherwise it will just return the input."
  [resolver input]
  (when (some? resolver)
    (if (satisfies? Resolvable resolver)
      (try
        (resolve-value resolver input)
        (catch Exception ex
          ; If the error already came from a resolve error then just throw it back up the stack
          ; no need to double handle it and mess with the AstSpan
          (if (resolve-error? ex) (throw ex)
              ; Otherwise wrap the error with the current AST node so that the error span can be calculated
              (throw-resolve-error (ex-message ex) resolver ex))))
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
