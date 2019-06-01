(ns ^{:doc "Syntax that represents arithmetic operations that can
            be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-arithmetic
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord AddOp [left :- Resolvable right :- Resolvable])
(s/defrecord SubOp [left :- Resolvable right :- Resolvable])
(s/defrecord MultOp [left :- Resolvable right :- Resolvable])
(s/defrecord DivOp [left :- Resolvable right :- Resolvable])
(s/defrecord ModOp [left :- Resolvable right :- Resolvable])
