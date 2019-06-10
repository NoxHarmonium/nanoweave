(ns ^{:doc "Syntax that represents manipulating the values available for operations."
      :author "Sean Dawson"}
 nanoweave.ast.scope
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord Binding [match :- Resolvable value :- Resolvable body :- Resolvable])
(s/defrecord Expression [body :- Resolvable])
(s/defrecord Indexing [target :- Resolvable key :- Resolvable])
(s/defrecord ImportOp [class-name :- Resolvable])
(s/defrecord When [clauses :- [Resolvable]])
(s/defrecord WhenClause [condition :- Resolvable body :- Resolvable])
