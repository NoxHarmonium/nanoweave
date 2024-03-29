(ns ^{:author "Sean Dawson"
      :doc "Parses operations that manipulate the values available in the scope of expressions."}
 nanoweave.parsers.scope
  (:require [blancas.kern.core :refer [<?> bind fail fwd look-ahead return]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [blancas.kern.lexer.java-style
             :refer
             [colon comma-sep string-lit sym token]]
            [nanoweave.ast.literals :refer [->BoolLit]]
            [nanoweave.ast.scope
             :refer
             [->Binding ->Expression ->ImportOp ->Indexing ->When ->WhenClause]]
            [nanoweave.parsers.pattern-matching :refer [binding-target]]
            [nanoweave.utils :refer [declare-extern]]))

; Forward declarations

(declare-extern nanoweave.parsers.expr/expr)

; Scopes

(def variable-binding
  "Parses a binding of an expression result to a target"
  (<s> (<?> (bind [target binding-target
                   _ (sym \=)
                   body (fwd nanoweave.parsers.expr/expr)
                   ps pop-span]
                  (return (partial (ps ->Binding) target body)))
            "variable binding")))
(def binding-list
  "Parses multiple variable bindings separated by commas to a sequence"
  (<?> (bind [bindings (comma-sep variable-binding)]
             (return (reduce comp bindings)))
       "binding list"))
(def let-scope
  "Creates a new scope that has variables that are bound in the binding list"
  (<s> (<?> (bind [_ (token "let")
                   bindings binding-list
                   _ colon
                   body (fwd nanoweave.parsers.expr/expr)
                   ps pop-span]
                  (return ((ps ->Expression) (bindings body))))
            "let statement")))
(def when-clause
  "An expression and an associated body that will be evaluated if the expression evaluates truthy"
  (<s> (<?> (bind [condition (fwd nanoweave.parsers.expr/expr)
                   _ colon
                   body (fwd nanoweave.parsers.expr/expr)
                   ps pop-span]
                  (return ((ps ->WhenClause) condition body)))
            "when clause")))
(def when-scope
  "A flow control construct that will take a branch if an expression evaluates truthy"
  (<s> (<?> (bind [_ (token "when")
                   clauses (comma-sep when-clause)
                   ps pop-span]
                  (return ((ps ->When) clauses)))
            "when statement")))
(def else
  "Always evaluates to true, used in a when clause to always execute a clause"
  (<s> (<?> (bind [_ (token "else")
                   ps pop-span]
                  (return ((ps ->BoolLit) true)))
            "else")))

(def indexing
  "Indexes a map or a sequence by a key.
  This parser is constructed differently from the others so
   that indexing can be treated as a binary operation
   without having an explicit operator."
  (bind [ahead (look-ahead (sym \[))]
        (if ahead
          (return ->Indexing)
          (fail "expected ["))))

(def import-statement
  "Imports a JVM class into scope"
  (<s> (<?> (bind [_ (token "import")
                   class string-lit
                   ps pop-span]
                  (return ((ps ->ImportOp) class)))
            "import")))

