(ns ^{:doc "Parses operations that match patterns and extract values from input"
      :author "Sean Dawson"}
 nanoweave.parsers.pattern-matching
  (:require [blancas.kern.core :refer [bind <|> <:> <?> fwd return]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [blancas.kern.lexer.java-style :refer
             [identifier colon brackets comma-sep braces token]]
            [nanoweave.parsers.text :refer [regex]]
            [nanoweave.ast.pattern-matching :refer
             [->LiteralMatchOp ->VariableMatchOp ->KeyMatchOp
              ->KeyValueMatchOp ->ListPatternMatchOp ->MapPatternMatchOp
              ->MatchClause ->Match ->RegexMatchOp]]
            [nanoweave.utils :refer [declare-extern]]))

; Forward declarations

(declare-extern nanoweave.parsers.expr/expr)

; Scopes

(declare binding-target)
(def literal-match
  "pareses an expression that pattern matches against a literal variable"
  (<s> (<?> (bind [target (fwd nanoweave.parsers.expr/expr)
                   ps pop-span]
                  (return ((ps ->LiteralMatchOp) target)))
            "literal pattern match")))
(def variable-match
  "parses an expression that pattern matches against a single variable"
  (<s> (<?> (bind [target identifier
                   ps pop-span]
                  (return ((ps ->VariableMatchOp) target)))
            "variable pattern match")))
(def regex-match
  "parses an expression that pattern matches with a regex expression"
  (<s> (<?> (bind [target regex
                   ps pop-span]
                  (return ((ps ->RegexMatchOp) target)))
            "regex pattern match")))
(def key-match
  "parses an expression that pattern matches against a key on a map"
  (<s> (<?> (bind [target identifier
                   ps pop-span]
                  (return ((ps ->KeyMatchOp) target)))
            "map key pattern match")))
(def key-value-match
  "parses an expression that pattern matches against a key/value pair"
  (<s> (<?> (bind [key key-match
                   _ colon
                   value (fwd binding-target)
                   ps pop-span]
                  (return ((ps ->KeyValueMatchOp) key value)))
            "map key/value pattern match")))
(def list-pattern-match
  "parses an expression that pattern matches against a list"
  (<s> (<?> (bind [_ (token "^")
                   match (brackets (comma-sep (fwd binding-target)))
                   ps pop-span]
                  (return ((ps ->ListPatternMatchOp) match)))
            "list pattern match")))
(def map-pattern-match
  "parses an expression that pattern matches against a map structure"
  (<s> (<?> (bind [_ (token "^")
                   match (braces (comma-sep (<|>
                                             (<:> key-value-match)
                                             key-match)))
                   ps pop-span]
                  (return ((ps ->MapPatternMatchOp) match)))
            "map pattern match")))

(def binding-target
  "parses the target of a variable binding"
  (<?> (<|>
        (<:> list-pattern-match)
        (<:> map-pattern-match)
        regex-match
        variable-match
        literal-match)
       "variable binding target"))

(def match-clause
  "A pattern match expression and an associated body that will be evaluated if the match succeeds"
  (<s> (<?> (bind [match binding-target
                   _ colon
                   body (fwd nanoweave.parsers.expr/expr)
                   ps pop-span]
                  (return ((ps ->MatchClause) match body)))
            "match clause")))
(def match-scope
  "A match construct will take a branch if a pattern matches and passes in the matched variables"
  (<s> (<?> (bind [_ (token "match")
                   clauses (braces (comma-sep match-clause))
                   ps pop-span]
                  (return (partial (ps ->Match) clauses)))
            "match statement")))
