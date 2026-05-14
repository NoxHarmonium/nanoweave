(ns ^{:doc "Syntax that represents arithmetic operations that can
            be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-arithmetic
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord AddOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord SubOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord MultOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord DivOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord ModOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
