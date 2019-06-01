(ns nanoweave.ast.lambda
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord Lambda [param-list :- [s/Str] body :- Resolvable])
(s/defrecord NoArgsLambda [body :- Resolvable])
(s/defrecord FunCall [target :- Resolvable args :- [Resolvable]])
(s/defrecord ArgList [arguments :- [Resolvable]])
