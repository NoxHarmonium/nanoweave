(ns ^{:author "Sean Dawson"
      :doc "Parses operations that manipulate the values available in the scope of expressions."}
 nanoweave.parsers.scope
  (:require [blancas.kern.core :refer [<?> bind fail look-ahead return]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [blancas.kern.lexer.java-style
             :refer
             [colon comma-sep string-lit sym token]]
            [nanoweave.ast.literals :refer [->BoolLit]]
            [nanoweave.ast.scope
             :refer
             [->Binding ->Expression ->ImportOp ->Indexing ->When ->WhenClause]]
            [nanoweave.parsers.pattern-matching :refer [make-binding-target]]))

; Forward declarations

; Scopes

(defn make-variable-binding
  "Parses a binding of an expression result to a target"
  [expr-p]
  (<s> (<?> (bind [target (make-binding-target expr-p)
                   _ (sym \=)
                   body expr-p
                   ps pop-span]
                  (return (partial (ps ->Binding) target body)))
            "variable binding")))
(defn make-binding-list
  "Parses multiple variable bindings separated by commas to a sequence"
  [expr-p]
  (<?> (bind [bindings (comma-sep (make-variable-binding expr-p))]
             (return (reduce comp bindings)))
       "binding list"))
(defn make-let-scope
  "Creates a new scope that has variables that are bound in the binding list"
  [expr-p]
  (<s> (<?> (bind [_ (token "let")
                   bindings (make-binding-list expr-p)
                   _ colon
                   body expr-p
                   ps pop-span]
                  (return ((ps ->Expression) (bindings body))))
            "let statement")))
(defn make-when-clause
  "An expression and an associated body that will be evaluated if the expression evaluates truthy"
  [expr-p]
  (<s> (<?> (bind [condition expr-p
                   _ colon
                   body expr-p
                   ps pop-span]
                  (return ((ps ->WhenClause) condition body)))
            "when clause")))
(defn make-when-scope
  "A flow control construct that will take a branch if an expression evaluates truthy"
  [expr-p]
  (<s> (<?> (bind [_ (token "when")
                   clauses (comma-sep (make-when-clause expr-p))
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
