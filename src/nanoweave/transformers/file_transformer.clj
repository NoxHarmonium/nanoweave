(ns ^{:doc "The transformer for nanoweave.", :author "Sean Dawson"}
 nanoweave.transformers.file-transformer
  (:require [blancas.kern.core :refer [parse]]
            [cheshire.core :refer [generate-string]]
            [nanoweave.ast.base :refer [wrap-uncaught-error]]
            [nanoweave.utils :refer [read-json-with-doubles]]
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
            [nanoweave.monadic.base :refer [domonad]]
            [nanoweave.monadic.either :refer [either-m ok err map-error]]))

(defn resolve-ast
  "Takes a parsed AST tree and transforms a given input with it"
  [ast input]
  (try
    (ok (safe-resolve-value ast input))
     ;; Resolve errors currently throw because it would take a pretty big refactor to
     ;; pass errors back out (probably would have to (ab)use something like the kern
     ;; state monad thing)
    (catch Exception ex
      (if-let [inner-error (unwrap-resolve-error ex)]
        (err inner-error)
        (err (wrap-uncaught-error :resolve-error ex ast))))))

(defn parse-nweave-definition
  "Takes a string with an nanoweave definition and parses it to an AST tree"
  [nweave-definition filename]
  (parse single-expression nweave-definition filename))

(defn transform
  "Transforms input with the given nanoweave definition and an optional
  transform function."
  ([input nweave filename]
   (let [pstate (parse-nweave-definition nweave filename)]
     (if (:ok pstate)
       (let [ast (:value pstate)
             result (resolve-ast ast {"input" input})]
         ; Ensure that the original input is associated with the error if possible
         ; This allows code frames to be rendered with format-error-with-context
         (map-error #(assoc %1 :input nweave) result))
       (err (convert-pstate-to-error-with-context pstate nweave))))))

(defn read-input-file!
  "Reads a given input file into a Clojure object for processing by Nanoweave."
  [input-file]
  (try
    (ok (read-json-with-doubles (io! (slurp input-file))))
    (catch Exception e
      (err (wrap-uncaught-error :nweave-read-error e nil)))))

(defn read-nweave-file!
  "Reads a given Nanoweave file for parsing."
  [nweave-file]
  (try
    (ok (io! (slurp nweave-file)))
    (catch Exception e
      (err (wrap-uncaught-error :input-read-error e nil)))))

(defn write-output-file!
  "Writes the transform results to a given file"
  [output-file xform-result]
  (try
    (let [output (generate-string xform-result)]
      (ok (io! (spit output-file output))))
    (catch Exception e
      (err (wrap-uncaught-error :output-error e nil)))))

(defn transform-files!
  "Transforms text from an input file with a given nanoweave
  defnition and writes the result to the output file"
  ([input-file output-file nweave-file]
   (#_{:clj-kondo/ignore [:unresolved-symbol]}
    (domonad either-m
             [input (read-input-file! input-file)
              nweave (read-nweave-file! nweave-file)
              result (transform input nweave nweave-file)
              _ (write-output-file! output-file result)]
             result))))
