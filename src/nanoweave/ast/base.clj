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

(def error-types (s/enum :resolve-error :parse-error :input-read-error :nweave-read-error :write-error))

(s/defrecord AstPos [line :- s/Int col :- s/Int src :- s/Str])
(s/defrecord AstSpan [start :- AstPos end :- AstPos])
(s/defrecord ErrorWithContext [message :- s/Str type :- error-types ast-node :- Resolvable span :- AstSpan cause :- Exception input :- s/Str])

(defn wrap-uncaught-error
  "In the unlikely case where an uncaught error slips through while resolving the AST
   this function will wrap that generic error in a contextual error.
   It won't be very useful because we don't know the node where the error was thrown."
  [error-type ex root-node]
  (->ErrorWithContext (ex-message ex) error-type root-node (:span root-node) ex nil))