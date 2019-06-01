(ns nanoweave.ast.binary-other
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord DotOp [left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [left :- Resolvable right :- Resolvable])
(s/defrecord OpenRangeOp [left :- Resolvable right :- Resolvable])
(s/defrecord ClosedRangeOp [left :- Resolvable right :- Resolvable])
