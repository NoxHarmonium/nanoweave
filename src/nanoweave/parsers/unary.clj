(ns ^{:doc "Parses miscellaneous operations that can be done on a single expression."
      :author "Sean Dawson"}
 nanoweave.parsers.unary
  (:require [blancas.kern.core :refer [bind <?> <|> return]]
            [blancas.kern.lexer.java-style :refer [one-of token]]
            [nanoweave.ast.unary :refer [->NotOp ->NegOp ->TypeOfOp]]))

; Unary Operators
(def wrapped-uni-op
  "Unary operators: not or negative."
  (<?> (bind [op (<|> (one-of "!-") (token "typeof"))]
             (return ({\! ->NotOp \- ->NegOp "typeof" ->TypeOfOp} op)))
       "unary operator (!,-)"))

;(s/defrecord TypeOfOp [value :- Resolvable])
