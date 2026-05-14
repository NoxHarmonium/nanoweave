(ns ^{:doc "Syntax that represents operations to define functions and call them."
      :author "Sean Dawson"}
 nanoweave.ast.lambda
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord Lambda [span :- AstSpan param-list :- [s/Str] body :- (s/protocol Resolvable)])
(s/defrecord NoArgsLambda [span :- AstSpan body :- (s/protocol Resolvable)])
(s/defrecord FunCall [span :- AstSpan target :- (s/protocol Resolvable) args :- (s/protocol Resolvable)])
(s/defrecord ArgList [span :- AstSpan arg-exprs :- [(s/protocol Resolvable)]])
