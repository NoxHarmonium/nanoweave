(ns ^{:author "Sean Dawson"
      :doc "Parses operations that manipulate text and strings."}
 nanoweave.parsers.text
  (:require [blancas.kern.core
             :refer
             [<+> <?> <|> between bind fwd many return sym* token*]]
            [blancas.kern.lexer.java-style :refer [lexeme]]
            [nanoweave.ast.scope :refer [->Expression]]
            [nanoweave.ast.text :refer [->InterpolatedString]]
            [nanoweave.parsers.custom-lexing :refer [string-char]]
            [nanoweave.utils :refer [declare-extern]]))

; Forward declarations

(declare-extern nanoweave.parsers.expr/expr)

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

