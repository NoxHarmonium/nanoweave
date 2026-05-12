(ns ^{:doc "Syntax that defines logic operations that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-logic
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord EqOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord NotEqOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord LessThanOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord LessThanEqOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord GrThanOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord GrThanEqOp [span :- AstSpan left :- Resolvable right :- Resolvable])

(s/defrecord AndOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord OrOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord XorOp [span :- AstSpan left :- Resolvable right :- Resolvable])
