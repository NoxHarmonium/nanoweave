(disable-warning
 {:linter :suspicious-expression
  :if-inside-macroexpansion-of #{'schema.core/defrecord}
  ; :arglists-for-linting '([])
  :reason "The defrecord macro expands to a redundant (but harmless) dissoc statement when creating a record with no attributes (in this case NilLit)."})
