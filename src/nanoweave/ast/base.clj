(ns nanoweave.ast.base
  "Defines the syntax that is parsed from the transform file.
  The tokens are parsed to first class records rather than just clojure functions
  so that in the future they can be extended with more features such as
  better error handling and type validation.")

(defprotocol Resolvable
  "Describes an AST node that can be resolved with an input.
  Used to transform AST trees into a final value."
  (resolve-value [this input] "Takes an input and resolves it to a file value."))

(defn safe-resolve-value
  "Resolves the input if the given resolver is not nil and conforms to Resolvable,
  otherwise it will just return the input."
  [resolver input]
  (if (and (some? resolver)
           (satisfies? Resolvable resolver))
    (resolve-value resolver input)
    (str (type resolver))))

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
