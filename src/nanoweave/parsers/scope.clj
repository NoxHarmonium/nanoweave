(ns ^{:doc "Parses operations that manipulate the values available
            in the scope of expressions."
      :author "Sean Dawson"}
 nanoweave.parsers.scope
  (:require [blancas.kern.core :refer [bind <|> <:> <+> <?> fwd return fail
                                       look-ahead token* sym* between many]]
            [blancas.kern.lexer.java-style :refer
             [identifier colon token comma-sep string-lit sym lexeme]]
            [nanoweave.ast.scope :refer
             [->Binding ->Expression ->WhenClause ->When
              ->Indexing ->ImportOp ->InterpolatedString]]
            [nanoweave.ast.literals :refer [->BoolLit]]
            [nanoweave.utils :refer [declare-extern]]
            [nanoweave.parsers.pattern-matching :refer [binding-target]]
            [nanoweave.parsers.custom-lexing :refer [string-char]]))

; Forward declarations

(declare-extern nanoweave.parsers.expr/expr)

; Scopes

(def variable-binding
  "Parses a binding of an expression result to a target"
  (<?> (bind [target binding-target
              _ (sym \=)
              body (fwd nanoweave.parsers.expr/expr)]
             (return (partial ->Binding target body)))
       "variable binding"))
(def binding-list
  "Parses multiple variable bindings separated by commas to a sequence"
  (<?> (bind [bindings (comma-sep variable-binding)]
             (return (reduce comp bindings)))
       "binding list"))
(def let-scope
  "Creates a new scope that has variables that are bound in the binding list"
  (<?> (bind [_ (token "let")
              bindings binding-list
              _ colon
              body (fwd nanoweave.parsers.expr/expr)]
             (return (->Expression (bindings body))))
       "let statement"))
(def when-clause
  "An expression and an associated body that will be evaluated if the expression evaluates truthy"
  (<?> (bind [condition (fwd nanoweave.parsers.expr/expr)
              _ colon
              body (fwd nanoweave.parsers.expr/expr)]
             (return (->WhenClause condition body)))
       "when clause"))
(def when-scope
  "A flow control construct that will take a branch if an expression evaluates truthy"
  (<?> (bind [_ (token "when")
              clauses (comma-sep when-clause)]
             (return (->When clauses)))
       "when statement"))
(def else
  "Always evaluates to true, used in a when clause to always execute a clause"
  (<?> (bind [_ (token "else")]
             (return (->BoolLit true)))
       "else"))

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
  (<?> (bind [_ (token "import")
              class string-lit]
             (return (->ImportOp class)))
       "import"))

; Interpolated String

(def interpolated-string-expression
  "Parses an expression embedded within a string"
  (<?> (bind [_ (token* "#{")
              body (fwd nanoweave.parsers.expr/expr)
              _ (token* "}")]
             (return (->Expression body))) "interpolated string expression"))
(def interpolated-string
  "Parses string literals and embedded expressions delimited by double quotes"
  (lexeme (between (sym* \")
                   (<?> (sym* \") "end string")
                   (many (<|>
                          interpolated-string-expression
                          (<+> (many (string-char [\" \#]))))))))
(def wrapped-interpolated-string
  "Wraps an interpolated-string parser so it returns an AST record rather than an array of strings and expressions."
  (<?> (bind [v interpolated-string]
             (return (->InterpolatedString v)))
       "string"))
