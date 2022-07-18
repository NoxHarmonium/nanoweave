(ns ^{:doc "Defines the base protocol for the syntax that is parsed from the transform file.
  The tokens are parsed to first class records rather than just clojure functions
  so that in the future they can be extended with more features such as
  better error handling and type validation.", :author "Sean Dawson"}
 nanoweave.ast.base
  (:require [schema.core :as s]))

(defprotocol Resolvable
  "Describes an AST node that can be resolved with an input.
  Used to transform AST trees into a final value."
  (resolve-value [this input] "Takes an input and resolves it to a final value."))

(s/defrecord AstPos [line :- s/Int col :- s/Int src :- s/Str])
(s/defrecord AstSpan [start :- AstPos end :- AstPos])
