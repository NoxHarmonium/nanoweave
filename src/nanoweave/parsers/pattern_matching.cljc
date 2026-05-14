(ns ^{:doc "Parses operations that match patterns and extract values from input"
      :author "Sean Dawson"}
 nanoweave.parsers.pattern-matching
  (:require [blancas.kern.core :refer [bind <|> <:> <?> return]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [blancas.kern.lexer.java-style :refer
             [identifier colon brackets comma-sep braces token]]
            [nanoweave.parsers.text :refer [regex]]
            [nanoweave.ast.pattern-matching :refer
             [->LiteralMatchOp ->VariableMatchOp ->KeyMatchOp
              ->KeyValueMatchOp ->ListPatternMatchOp ->MapPatternMatchOp
              ->MatchClause ->Match ->RegexMatchOp]]))

; Scopes

; Note: parsers starting with make- are "factory parsers" which take an `expr` parser and return
; a parser that can parse expressions. This is to get around cross namespace circular references.

(defn make-literal-match
  "pareses an expression that pattern matches against a literal variable"
  [expr-p]
  (<s> (<?> (bind [target expr-p
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
(defn make-key-value-match
  "parses an expression that pattern matches against a key/value pair"
  [bt-p]
  (<s> (<?> (bind [key key-match
                   _ colon
                   value bt-p
                   ps pop-span]
                  (return ((ps ->KeyValueMatchOp) key value)))
            "map key/value pattern match")))
(defn make-list-pattern-match
  "parses an expression that pattern matches against a list"
  [bt-p]
  (<s> (<?> (bind [_ (token "^")
                   match (brackets (comma-sep bt-p))
                   ps pop-span]
                  (return ((ps ->ListPatternMatchOp) match)))
            "list pattern match")))
(defn make-map-pattern-match
  "parses an expression that pattern matches against a map structure"
  [bt-p]
  (<s> (<?> (bind [_ (token "^")
                   match (braces (comma-sep (<|>
                                             (<:> (make-key-value-match bt-p))
                                             key-match)))
                   ps pop-span]
                  (return ((ps ->MapPatternMatchOp) match)))
            "map pattern match")))

(defn make-binding-target
  "parses the target of a variable binding"
  [expr-p]
  (let [bt (atom nil)
        bt-p (fn [s] (@bt s))]
    (reset! bt
            (<?> (<|>
                  (<:> (make-list-pattern-match bt-p))
                  (<:> (make-map-pattern-match bt-p))
                  regex-match
                  variable-match
                  (make-literal-match expr-p))
                 "variable binding target"))
    @bt))

(defn make-match-clause
  "A pattern match expression and an associated body that will be evaluated if the match succeeds"
  [expr-p]
  (<s> (<?> (bind [match (make-binding-target expr-p)
                   _ colon
                   body expr-p
                   ps pop-span]
                  (return ((ps ->MatchClause) match body)))
            "match clause")))
(defn make-match-scope
  "A match construct will take a branch if a pattern matches and passes in the matched variables"
  [expr-p]
  (<s> (<?> (bind [_ (token "match")
                   clauses (braces (comma-sep (make-match-clause expr-p)))
                   ps pop-span]
                  (return (partial (ps ->Match) clauses)))
            "match statement")))
