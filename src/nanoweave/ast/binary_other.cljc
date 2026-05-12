(ns ^{:doc "Syntax that represents miscellaneous operations that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-other
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord DotOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord OpenRangeOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord ClosedRangeOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord IsOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord AsOp [span :- AstSpan left :- Resolvable right :- Resolvable])
