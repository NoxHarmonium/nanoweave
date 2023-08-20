(ns ^{:doc "Parses miscellaneous operations that can be done on a single expression."
      :author "Sean Dawson"}
 nanoweave.parsers.unary
  (:require [blancas.kern.core :refer [bind <?> <|> return]]
            [blancas.kern.lexer.java-style :refer [one-of token]]
            [nanoweave.parsers.base :refer [<s> pop-span]]
            [nanoweave.ast.unary :refer [->NotOp ->NegOp ->TypeOfOp]]))

; Unary Operators
(def wrapped-uni-op
  "Unary operators: not or negative."
  (<s> (<?> (bind [op (<|> (one-of "!-") (token "typeof"))
                   ps pop-span]
                  (return ({\! (ps ->NotOp) \- (ps ->NegOp) "typeof" (ps ->TypeOfOp)} op)))
            "unary operator (!,-)")))
