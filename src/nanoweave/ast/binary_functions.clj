(ns nanoweave.ast.binary-functions
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord MapOp [left :- Resolvable right :- Resolvable])
(s/defrecord FilterOp [left :- Resolvable right :- Resolvable])
(s/defrecord ReduceOp [left :- Resolvable right :- Resolvable])
