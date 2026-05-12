(ns ^{:doc "Syntax that match patterns and extract values from input."
      :author "Sean Dawson"}
 nanoweave.ast.pattern-matching
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord ListPatternMatchOp [span :- AstSpan targets :- [Resolvable]])
(s/defrecord MapPatternMatchOp [span :- AstSpan targets :- [Resolvable]])
(s/defrecord RegexMatchOp [span :- AstSpan pattern :- Resolvable])
(s/defrecord VariableMatchOp [span :- AstSpan target :- Resolvable])
(s/defrecord LiteralMatchOp [span :- AstSpan target :- Resolvable])

(s/defrecord KeyMatchOp [span :- AstSpan target :- Resolvable])
(s/defrecord KeyValueMatchOp [span :- AstSpan key :- Resolvable value :- Resolvable])
(s/defrecord Match [span :- AstSpan clauses :- [Resolvable] target :- Resolvable])
(s/defrecord MatchClause [span :- AstSpan match :- Resolvable body :- Resolvable])
