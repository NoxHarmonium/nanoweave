(ns ^{:doc "Unary parsers.", :author "Sean Dawson"}
 nanoweave.parsers.unary
  (:require [blancas.kern.core :refer [bind <?> return]]
            [blancas.kern.lexer.java-style :refer [one-of]]
            [nanoweave.ast.unary :refer [->NotOp ->NegOp]]))

; Unary Operators
(def wrapped-uni-op
  "Unary operators: not or negative."
  (<?> (bind [op (one-of "!-")]
             (return ({\! ->NotOp \- ->NegOp} op)))
       "unary operator (!,-)"))