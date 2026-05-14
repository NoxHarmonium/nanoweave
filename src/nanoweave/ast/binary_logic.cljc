(ns ^{:doc "Syntax that defines logic operations that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-logic
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord EqOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord NotEqOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord LessThanOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord LessThanEqOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord GrThanOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord GrThanEqOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])

(s/defrecord AndOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord OrOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord XorOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
