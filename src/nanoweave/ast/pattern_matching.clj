(ns ^{:doc "Syntax that match patterns and extract values from input."
      :author "Sean Dawson"}
 nanoweave.ast.pattern-matching
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord ListPatternMatchOp [targets :- [Resolvable]])
(s/defrecord MapPatternMatchOp [targets :- [Resolvable]])
(s/defrecord RegexMatchOp [pattern :- Resolvable])
(s/defrecord VariableMatchOp [target :- Resolvable])
(s/defrecord LiteralMatchOp [target :- Resolvable])

(s/defrecord KeyMatchOp [target :- Resolvable])
(s/defrecord KeyValueMatchOp [key :- Resolvable value :- Resolvable])
(s/defrecord Match [clauses :- [Resolvable] target :- Resolvable])
(s/defrecord MatchClause [match :- Resolvable body :- Resolvable])
