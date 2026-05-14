(ns ^{:doc "Syntax that represents miscellaneous operations that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-other
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord DotOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord ConcatOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord OpenRangeOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord ClosedRangeOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord IsOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
(s/defrecord AsOp [span :- AstSpan left :- (s/protocol Resolvable) right :- (s/protocol Resolvable)])
