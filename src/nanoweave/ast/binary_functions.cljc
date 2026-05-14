(ns ^{:doc "Syntax that represents functional programming style operations
            that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-functions
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord MapOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord FilterOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord ReduceOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])

(s/defrecord RegexMatchOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord RegexFindOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord RegexSplitOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
