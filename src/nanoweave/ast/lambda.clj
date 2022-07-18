(ns ^{:doc "Syntax that represents operations to define functions and call them."
      :author "Sean Dawson"}
 nanoweave.ast.lambda
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord Lambda [span :- AstSpan param-list :- [s/Str] body :- Resolvable])
(s/defrecord NoArgsLambda [span :- AstSpan body :- Resolvable])
(s/defrecord FunCall [span :- AstSpan target :- Resolvable args :- [Resolvable]])
(s/defrecord ArgList [span :- AstSpan arguments :- [Resolvable]])
