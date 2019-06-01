(ns nanoweave.ast.binary-logic
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord EqOp [left :- Resolvable right :- Resolvable])
(s/defrecord NotEqOp [left :- Resolvable right :- Resolvable])
(s/defrecord LessThanOp [left :- Resolvable right :- Resolvable])
(s/defrecord LessThanEqOp [left :- Resolvable right :- Resolvable])
(s/defrecord GrThanOp [left :- Resolvable right :- Resolvable])
(s/defrecord GrThanEqOp [left :- Resolvable right :- Resolvable])

(s/defrecord AndOp [left :- Resolvable right :- Resolvable])
(s/defrecord OrOp [left :- Resolvable right :- Resolvable])
(s/defrecord XorOp [left :- Resolvable right :- Resolvable])
