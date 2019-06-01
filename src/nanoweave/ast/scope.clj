(ns nanoweave.ast.scope
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord Binding [match :- Resolvable value :- Resolvable body :- Resolvable])
(s/defrecord Expression [body :- Resolvable])
(s/defrecord InterpolatedString [body :- [Resolvable]])
(s/defrecord Indexing [target :- Resolvable key :- Resolvable])
(s/defrecord ImportOp [class-name :- Resolvable])
(s/defrecord ListPatternMatchOp [targets :- [Resolvable]])
(s/defrecord MapPatternMatchOp [targets :- [Resolvable]])
(s/defrecord VariableMatchOp [target :- Resolvable])
(s/defrecord LiteralMatchOp [target :- Resolvable])
(s/defrecord KeyMatchOp [target :- Resolvable])
(s/defrecord KeyValueMatchOp [key :- Resolvable value :- Resolvable])
(s/defrecord When [clauses :- [Resolvable]])
(s/defrecord WhenClause [condition :- Resolvable body :- Resolvable])
(s/defrecord Match [clauses :- [Resolvable] target :- Resolvable])
(s/defrecord MatchClause [match :- Resolvable body :- Resolvable])
