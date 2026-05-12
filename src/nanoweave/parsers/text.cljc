(ns ^{:author "Sean Dawson"
      :doc "Parses operations that manipulate text and strings."}
 nanoweave.parsers.text
  (:require [blancas.kern.core
             :refer
             [<+> <?> <|> between bind many return sym* token* fail]]
            [blancas.kern.lexer.java-style :refer [lexeme]]
            [nanoweave.ast.scope :refer [->Expression]]
            [nanoweave.ast.text :refer [->InterpolatedString ->Regex]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [nanoweave.parsers.custom-lexing :refer [string-char regex-char]]))

; Interpolated String

; Note: parsers starting with make- are "factory parsers" which take an `expr` parser and return
; a parser that can parse expressions. This is to get around cross namespace circular references.

(defn make-interpolated-string-expression
  "Parses an expression embedded within a string"
  [expr-p]
  (<s> (<?> (bind [_ (token* "#{")
                   body expr-p
                   _ (token* "}")
                   ps pop-span]
                  (return ((ps ->Expression) body))) "interpolated string expression")))
(defn make-interpolated-string
  "Parses string literals and embedded expressions delimited by double quotes"
  [expr-p]
  (lexeme (between (sym* \")
                   (<?> (sym* \") "end string")
                   (many (<|>
                          (make-interpolated-string-expression expr-p)
                          (<+> (many (string-char [\" \#]))))))))
(defn make-wrapped-interpolated-string
  "Wraps an interpolated-string parser so it returns an AST record rather than an array of strings and expressions."
  [expr-p]
  (<s> (<?> (bind [v (make-interpolated-string expr-p)
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
                                (catch #?(:clj Exception :cljs :default) e (fail (ex-message e)))))))))
