(ns ^{:doc "Parses arithmetic operations that can be done on two expressions.", :author "Sean Dawson"}
 nanoweave.parsers.binary-arithmetic
  (:require [blancas.kern.core :refer [bind <?> return]]
            [blancas.kern.lexer.java-style :refer [one-of]]
            [nanoweave.ast.binary-arithmetic :refer
             [->MultOp ->DivOp ->ModOp ->AddOp ->SubOp]]))

; Arithmetic Binary Operators

(def wrapped-mul-op
  "Multiplicative operator: multiplication, division, or modulo."
  (<?> (bind [op (one-of "*/%")]
             (return ({\* ->MultOp \/ ->DivOp \% ->ModOp} op)))
       "multiplication operator (*,/,%)"))
(def wrapped-add-op
  "Additive operator: addition or subtraction."
  (<?> (bind [op (one-of "+-")]
             (return ({\+ ->AddOp \- ->SubOp} op)))
       "addition operator (+,-)"))
