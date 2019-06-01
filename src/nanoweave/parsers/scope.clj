(ns ^{:doc "Parses operations that manipulate the values available
            in the scope of expressions."
      :author "Sean Dawson"}
 nanoweave.parsers.scope
  (:require [blancas.kern.core :refer [bind <|> <:> <+> <?> fwd return fail
                                       look-ahead token* sym* between many]]
            [blancas.kern.lexer.java-style :refer
             [identifier colon token brackets comma-sep
              braces parens string-lit sym lexeme]]
            [nanoweave.ast.scope :refer
             [->LiteralMatchOp ->VariableMatchOp ->KeyMatchOp
              ->KeyValueMatchOp ->ListPatternMatchOp ->MapPatternMatchOp
              ->Binding ->Expression ->WhenClause ->When
              ->MatchClause ->Match ->Indexing ->ImportOp ->InterpolatedString]]
            [nanoweave.ast.literals :refer [->BoolLit]]
            [nanoweave.utils :refer [declare-extern]]
            [nanoweave.parsers.custom-lexing :refer [string-char]]))

; Forward declarations

(declare-extern nanoweave.parsers.expr/expr)

; Scopes

(declare binding-target)
(def literal-match
  "pareses an expression that pattern matches against a literal variable"
  (<?> (bind [target (fwd nanoweave.parsers.expr/expr)]
             (return (->LiteralMatchOp target)))
       "literal pattern match"))
(def variable-match
  "parses an expression that pattern matches against a single variable"
  (<?> (bind [target identifier]
             (return (->VariableMatchOp target)))
       "variable pattern match"))
(def key-match
  "parses an expression that pattern matches against a key on a map"
  (<?> (bind [target identifier]
             (return (->KeyMatchOp target)))
       "map key pattern match"))
(def key-value-match
  "parses an expression that pattern matches against a key/value pair"
  (<?> (bind [key key-match
              _ colon
              value (fwd binding-target)]
             (return (->KeyValueMatchOp key value)))
       "map key/value pattern match"))
(def list-pattern-match
  "parses an expression that pattern matches against a list"
  (<?> (bind [_ (token "^")
              match (brackets (comma-sep (fwd binding-target)))]
             (return (->ListPatternMatchOp match)))
       "list pattern match"))
(def map-pattern-match
  "parses an expression that pattern matches against a map structure"
  (<?> (bind [_ (token "^")
              match (braces (comma-sep (<|>
                                        (<:> key-value-match)
                                        key-match)))]
             (return (->MapPatternMatchOp match)))
       "map pattern match"))

(def binding-target
  "parses the target of a variable binding"
  (<?> (<|>
        (<:> list-pattern-match)
        (<:> map-pattern-match)
        variable-match
        literal-match)
       "variable binding target"))

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
(def match-clause
  "A pattern match expression and an associated body that will be evaluated if the match succeeds"
  (<?> (bind [match binding-target
              _ colon
              body (fwd nanoweave.parsers.expr/expr)]
             (return (->MatchClause match body)))
       "match clause"))
(def match-scope
  "A match construct will take a branch if a pattern matches and passes in the matched variables"
  (<?> (bind [_ (token "match")
              clauses (parens (comma-sep match-clause))]
             (return (partial ->Match clauses)))
       "match statement"))

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
