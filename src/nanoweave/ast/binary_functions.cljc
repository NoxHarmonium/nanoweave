(ns ^{:doc "Syntax that represents functional programming style operations
            that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-functions
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord MapOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord FilterOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord ReduceOp [span :- AstSpan left :- Resolvable right :- Resolvable])

(s/defrecord RegexMatchOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord RegexFindOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord RegexSplitOp [span :- AstSpan left :- Resolvable right :- Resolvable])
