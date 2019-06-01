(ns ^{:doc "Syntax that represents functional programming style operations
            that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-functions
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord MapOp [left :- Resolvable right :- Resolvable])
(s/defrecord FilterOp [left :- Resolvable right :- Resolvable])
(s/defrecord ReduceOp [left :- Resolvable right :- Resolvable])
