(ns ^{:doc "Syntax that match patterns and extract values from input."
      :author "Sean Dawson"}
 nanoweave.ast.pattern-matching
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord ListPatternMatchOp [span :- AstSpan targets :- [(s/protocol Resolvable)]])
(s/defrecord MapPatternMatchOp [span :- AstSpan targets :- [(s/protocol Resolvable)]])
(s/defrecord RegexMatchOp [span :- AstSpan pattern :- (s/protocol Resolvable)])
(s/defrecord VariableMatchOp [span :- AstSpan target :- s/Str])
(s/defrecord LiteralMatchOp [span :- AstSpan target :- (s/protocol Resolvable)])

(s/defrecord KeyMatchOp [span :- AstSpan target :- s/Str])
(s/defrecord KeyValueMatchOp [span :- AstSpan key :- (s/protocol Resolvable) value :- (s/protocol Resolvable)])
(s/defrecord MatchClause [span :- AstSpan match :- (s/protocol Resolvable) body :- (s/protocol Resolvable)])
(s/defrecord Match [span :- AstSpan clauses :- [MatchClause] target :- (s/protocol Resolvable)])
