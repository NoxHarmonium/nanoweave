(ns ^{:doc "Syntax that represents miscellaneous operations that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-other
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord DotOp [left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [left :- Resolvable right :- Resolvable])
(s/defrecord OpenRangeOp [left :- Resolvable right :- Resolvable])
(s/defrecord ClosedRangeOp [left :- Resolvable right :- Resolvable])
(s/defrecord IsOp [left :- Resolvable right :- Resolvable])
(s/defrecord AsOp [left :- Resolvable right :- Resolvable])
