(ns nanoweave.ast.operators)

; Thanks https://github.com/mikera/core.matrix
(defn xor
  "Returns the logical xor of a set of values, considered as booleans"
  ([] false)
  ([x] (boolean x))
  ([x y] (if x (not y) (boolean y))))