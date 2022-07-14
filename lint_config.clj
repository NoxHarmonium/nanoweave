; TODO: I can't get this to work
(disable-warning
 {:linter :suspicious-expression
  :for-macro 'clojure.core/dissoc
  :if-inside-macroexpansion-of #{'schema.core/defrecord}
  :within-depth 20
  :reason "The defrecord macro expands to a redundant (but harmless) dissoc statement when creating a record with no attributes (in this case NilLit)."})
