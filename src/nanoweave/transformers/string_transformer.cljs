(ns nanoweave.transformers.string-transformer
  (:require [blancas.kern.core :refer [parse]]
            [nanoweave.ast.base :refer [wrap-uncaught-error]]
            [nanoweave.io-utils :refer [read-json-with-doubles]]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.parsers.expr :refer [single-expression]]
            [nanoweave.parsers.errors :refer [convert-pstate-to-error-with-context]]
            [nanoweave.resolvers.errors :refer [unwrap-resolve-error]]
            [nanoweave.resolvers.binary-arithmetic]
            [nanoweave.resolvers.binary-functions]
            [nanoweave.resolvers.binary-logic]
            [nanoweave.resolvers.binary-other]
            [nanoweave.resolvers.lambda]
            [nanoweave.resolvers.literals]
            [nanoweave.resolvers.operators]
            [nanoweave.resolvers.primatives]
            [nanoweave.resolvers.pattern-matching]
            [nanoweave.resolvers.scope]
            [nanoweave.resolvers.text]
            [nanoweave.resolvers.unary]
            [nanoweave.monadic.either :refer [ok err map-error]]))

(defn- resolve-ast
  [ast input]
  (try
    (ok (safe-resolve-value ast input))
    (catch :default ex
      (if-let [inner-error (unwrap-resolve-error ex)]
        (err inner-error)
        (err (wrap-uncaught-error :resolve-error ex ast))))))

(defn transform-strings
  "Takes an input JSON string and a nanoweave transform string,
   parses and resolves them, and returns an either monad result.
   On success the value is a JS-serialisable Clojure value.
   On failure the value is an ErrorWithContext record."
  [input-str transform-str]
  (let [pstate (parse single-expression transform-str "<string>")]
    (if (:ok pstate)
      (let [ast (:value pstate)
            input (read-json-with-doubles input-str)
            result (resolve-ast ast {"input" input})]
        (map-error #(assoc % :input transform-str) result))
      (err (convert-pstate-to-error-with-context pstate transform-str)))))
