(ns ^{:doc "Syntax that represents arithmetic operations that can
            be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-arithmetic
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord AddOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord SubOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord MultOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord DivOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord ModOp [span :- AstSpan left :- Resolvable right :- Resolvable])
