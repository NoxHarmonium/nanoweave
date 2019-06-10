(ns nanoweave.transformers.file-transformer
  (:require [blancas.kern.core :refer [parse]]
            [cheshire.core :refer [generate-string]]
            [nanoweave.parsers.expr :refer [expr]]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.transformers.errors :refer [format-error]]
            [nanoweave.utils :refer [read-json-with-doubles]]))

(defn resolve-ast
  "Takes a parsed AST tree and transforms a given input with it"
  [ast input]
  (safe-resolve-value ast input))

(defn parse-nweave-definition
  "Takes a string with an nanoweave definition and parses it to an AST tree"
  [nweave-definition]
  (parse expr nweave-definition))

(defn transform
  "Transforms input with the given nanoweave definition and an optional
  transform function."
  ([input nweave]
   (transform input nweave resolve-ast))
  ([input nweave transform-fn]
   (let [pstate (parse-nweave-definition nweave)]
     (if (:ok pstate)
       (let [ast (:value pstate)]
         (transform-fn ast {"input" input}))
       (throw (AssertionError. (format-error pstate)))))))

(defn transform-files
  "Transforms text from an input file with a given nanoweave
  defnition and writes the result to the output file"
  ([input-file output-file nweave-file]
   (let [input (read-json-with-doubles (slurp input-file))
         nweave (slurp nweave-file)
         output (generate-string (transform input nweave))]
     (spit output-file output))))
