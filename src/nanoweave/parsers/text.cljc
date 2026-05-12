(ns ^{:author "Sean Dawson"
      :doc "Parses operations that manipulate text and strings."}
 nanoweave.parsers.text
  (:require [blancas.kern.core
             :refer
             [<+> <?> <|> between bind fwd many return sym* token* fail]]
            [blancas.kern.lexer.java-style :refer [lexeme]]
            [nanoweave.ast.scope :refer [->Expression]]
            [nanoweave.ast.text :refer [->InterpolatedString ->Regex]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [nanoweave.parsers.custom-lexing :refer [string-char regex-char]]
            [nanoweave.utils :refer [declare-extern]]))

; Forward declarations

(declare-extern nanoweave.parsers.expr/expr)

; Interpolated String

(def interpolated-string-expression
  "Parses an expression embedded within a string"
  (<s> (<?> (bind [_ (token* "#{")
                   body (fwd nanoweave.parsers.expr/expr)
                   _ (token* "}")
                   ps pop-span]
                  (return ((ps ->Expression) body))) "interpolated string expression")))
(def interpolated-string
  "Parses string literals and embedded expressions delimited by double quotes"
  (lexeme (between (sym* \")
                   (<?> (sym* \") "end string")
                   (many (<|>
                          interpolated-string-expression
                          (<+> (many (string-char [\" \#]))))))))
(def wrapped-interpolated-string
  "Wraps an interpolated-string parser so it returns an AST record rather than an array of strings and expressions."
  (<s> (<?> (bind [v interpolated-string
                   ps pop-span]
                  (return ((ps ->InterpolatedString) v)))
            "string")))
(def regex
  "Parses regular expression delimited by forward slashes"
  (<s> (lexeme (between (sym* \/)
                        (<?> (sym* \/) "end regex")
                        (bind [pattern (<+> (many (regex-char)))
                               ps pop-span]
                              (try
                                (return ((ps ->Regex) (re-pattern pattern)))
                                (catch Exception e (fail (.getMessage e)))))))))

