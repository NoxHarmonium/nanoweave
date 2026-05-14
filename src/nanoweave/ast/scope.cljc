(ns ^{:doc "Syntax that represents manipulating the values available for operations."
      :author "Sean Dawson"}
 nanoweave.ast.scope
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord Binding [span :- AstSpan match :- (s/protocol Resolvable) value :- (s/protocol Resolvable) body :- (s/protocol Resolvable)])
(s/defrecord Expression [span :- AstSpan body :- (s/protocol Resolvable)])
(s/defrecord Indexing [span :- AstSpan target :- (s/protocol Resolvable) key :- (s/protocol Resolvable)])
(s/defrecord ImportOp [span :- AstSpan class-name :- (s/protocol Resolvable)])
(s/defrecord WhenClause [span :- AstSpan condition :- (s/protocol Resolvable) body :- (s/protocol Resolvable)])
(s/defrecord When [span :- AstSpan clauses :- [WhenClause]])
