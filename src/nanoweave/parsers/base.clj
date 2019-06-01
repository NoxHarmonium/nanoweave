(ns ^{:doc "Base parsers.", :author "Sean Dawson"}
 nanoweave.parsers.base
  (:require [blancas.kern.core :refer [bind <|> <?> fwd return]]
            [blancas.kern.lexer.java-style :refer
              [colon brackets braces comma-sep string-lit identifier]]
            [nanoweave.ast.literals :refer [->ArrayLit]]
            [nanoweave.utils :refer [declare-extern]]))

; Forward declarations

(declare-extern nanoweave.parser.definitions/expr)

; JSON Elements

(def pair
  "Parses the rule:  pair := String ':' expr"
  (<?> (bind [key (<|> string-lit identifier)
              _ colon
              value (fwd nanoweave.parser.definitions/expr)] (return [key value]))
       "pair"))
(def array
  "Parses the rule:  array := '[' (expr (',' expr)*)* ']'"
  (<?> (brackets (bind [members (comma-sep (fwd nanoweave.parser.definitions/expr))]
                       (return (->ArrayLit members))))
       "array"))
(def object
  "Parses the rule:  object := '{' (pair (',' pair)*)* '}'"
  (<?> (braces (bind [members (comma-sep pair)]
                     (return (apply hash-map (reduce concat [] members)))))
       "object"))