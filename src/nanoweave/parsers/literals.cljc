(ns ^{:doc "Pareses literal values."
      :author "Sean Dawson"}
 nanoweave.parsers.literals
  (:require [blancas.kern.core :refer [bind <?> <|> fwd optional sym* return]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [blancas.kern.lexer.java-style :refer
             [identifier float-lit bool-lit nil-lit token braces brackets colon comma-sep identifier string-lit]]
            [nanoweave.ast.literals :refer [->IdentiferLit ->FloatLit ->BoolLit ->NilLit ->TypeLit ->PairLit ->ArrayLit ->ObjectLit]]))

; 'JSON' Style Elements

(def pair-lit
  "Parses the rule:  pair := String ':' expr"
  (<s> (<?> (bind [key (<|> string-lit identifier)
                   _ colon
                   value (fwd nanoweave.parsers.expr/expr)
                   ps pop-span] (return ((ps ->PairLit) key value)))
            "pair")))
(def array-lit
  "Parses the rule:  array := '[' (expr (',' expr)*)* ']'"
  (<s> (<?> (brackets (bind [members (comma-sep (fwd nanoweave.parsers.expr/expr))
                             ps pop-span]
                            (return ((ps ->ArrayLit) members))))
            "array")))
(def object-lit
  "Parses the rule:  object := '{' (pair (',' pair)*)* '}'"
  (<s> (<?> (braces (bind [members (comma-sep pair-lit)
                           ps pop-span]
                          (return ((ps ->ObjectLit) members))))
            "object")))

; Wrapped Primitives

(def wrapped-identifier
  (<s> (<?> (bind [static-prefix (optional (sym* \$))
                   v identifier
                   ps pop-span]
                  (return ((ps ->IdentiferLit) v (some? static-prefix))))
            "identifier")))
(def wrapped-float-lit
  "Wraps a float-lit parser so it returns an AST record rather than a float."
  (<s> (<?> (bind [v float-lit
                   ps pop-span]
                  (return ((ps ->FloatLit) (double v))))
            "float")))
(def wrapped-bool-lit
  "Wraps a bool-lit parser so it returns an AST record rather than a bool."
  (<s> (<?> (bind [v bool-lit
                   ps pop-span] (return ((ps ->BoolLit) v)))
            "boolean")))
(def wrapped-nil-lit
  "Wraps an nil-lit parser so it returns an AST record rather than a null."
  (<s> (<?> (bind [_ nil-lit
                   ps pop-span] (return ((ps ->NilLit))))
            "null")))
(def type-lit
  "Parses a nanoweave type literal"
  (<s> (<?> (bind [type-name (token "Number" "String" "Boolean" "Nil" "Array")
                   ps pop-span]
                  (return ((ps ->TypeLit) type-name)))
            "type")))

