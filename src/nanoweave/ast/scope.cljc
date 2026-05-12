(ns ^{:doc "Syntax that represents manipulating the values available for operations."
      :author "Sean Dawson"}
 nanoweave.ast.scope
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord Binding [span :- AstSpan match :- Resolvable value :- Resolvable body :- Resolvable])
(s/defrecord Expression [span :- AstSpan body :- Resolvable])
(s/defrecord Indexing [span :- AstSpan target :- Resolvable key :- Resolvable])
(s/defrecord ImportOp [span :- AstSpan class-name :- Resolvable])
(s/defrecord When [span :- AstSpan clauses :- [Resolvable]])
(s/defrecord WhenClause [span :- AstSpan condition :- Resolvable body :- Resolvable])
