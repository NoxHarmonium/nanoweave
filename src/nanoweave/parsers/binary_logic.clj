(ns ^{:doc "Parses logic operations that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.parsers.binary-logic
  (:require [blancas.kern.core :refer [bind <?> return]]
            [blancas.kern.lexer.java-style :refer [token]]
            [nanoweave.ast.binary-logic :refer
             [->GrThanEqOp ->LessThanEqOp ->GrThanOp ->LessThanOp
              ->EqOp ->NotEqOp ->AndOp ->OrOp ->XorOp]]))


; Logical Binary Operators


(def wrapped-rel-op
  "Relational operator: greater than, less than"
  (<?> (bind [op (token ">=" "<=" ">" "<")]
             (return ({">=" ->GrThanEqOp "<=" ->LessThanEqOp ">" ->GrThanOp "<" ->LessThanOp} op)))
       "relational operator (>=,<=,>,<)"))
(def wrapped-eq-op
  "Equality operator: equal or not-equal"
  (<?> (bind [op (token "==" "!=")]
             (return ({"==" ->EqOp "!=" ->NotEqOp} op)))
       "equality operator (==,!=)"))
(def wrapped-and-op
  "Logical AND operator"
  (<?> (bind [op (token "and")]
             (return ({"and" ->AndOp} op)))
       "and operator"))
(def wrapped-or-op
  "Logical OR operator"
  (<?> (bind [op (token "or")]
             (return ({"or" ->OrOp} op)))
       "or operator"))
(def wrapped-xor-op
  "Logical XOR operator"
  (<?> (bind [op (token "xor")]
             (return ({"xor" ->XorOp} op)))
       "xor operator"))