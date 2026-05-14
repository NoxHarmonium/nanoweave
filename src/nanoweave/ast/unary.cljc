(ns ^{:doc "Syntax that represents miscellaneous operations that can be done on a single expression."
      :author "Sean Dawson"}
 nanoweave.ast.unary
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord NotOp [span :- AstSpan value :- (s/protocol Resolvable)])
(s/defrecord NegOp [span :- AstSpan value :- (s/protocol Resolvable)])
(s/defrecord TypeOfOp [span :- AstSpan value :- (s/protocol Resolvable)])
