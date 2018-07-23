(ns nanoweave.ast.binary-logic
  (:require [schema.core :as s])
  (:use nanoweave.ast.base)
  (:use [nanoweave.parser.operators :only [xor]]))

(s/defrecord EqOp [left :- Resolvable right :- Resolvable])
(s/defrecord NotEqOp [left :- Resolvable right :- Resolvable])
(s/defrecord LessThanOp [left :- Resolvable right :- Resolvable])
(s/defrecord LessThanEqOp [left :- Resolvable right :- Resolvable])
(s/defrecord GrThanOp [left :- Resolvable right :- Resolvable])
(s/defrecord GrThanEqOp [left :- Resolvable right :- Resolvable])

(s/defrecord AndOp [left :- Resolvable right :- Resolvable])
(s/defrecord OrOp [left :- Resolvable right :- Resolvable])
(s/defrecord XorOp [left :- Resolvable right :- Resolvable])

(extend-protocol Resolvable
  EqOp
  (resolve-value [this input] (handle-bin-op this input =))
  NotEqOp
  (resolve-value [this input] (handle-bin-op this input not=))
  LessThanOp
  (resolve-value [this input] (handle-bin-op this input <))
  LessThanEqOp
  (resolve-value [this input] (handle-bin-op this input <=))
  GrThanOp
  (resolve-value [this input] (handle-bin-op this input >))
  GrThanEqOp
  (resolve-value [this input] (handle-bin-op this input >=))
  AndOp
  (resolve-value [this input] (handle-bin-op this input #(and %1 %2)))
  OrOp
  (resolve-value [this input] (handle-bin-op this input #(or %1 %2)))
  XorOp
  (resolve-value [this input] (handle-bin-op this input xor)))
