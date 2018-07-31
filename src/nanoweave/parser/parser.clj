(ns nanoweave.parser.parser
  ^{:doc "The parser for nanoweave.", :author "Sean Dawson"}
  (:use [clojure.walk :only [prewalk]]
        [clojure.pprint])
  (:require [blancas.kern.core :as kern]
            [clojure.data.json :as json]
            [nanoweave.utils :refer [read-json-with-doubles]]
            [nanoweave.ast.base :as ast]
            [nanoweave.parser.definitions :as def]
            [nanoweave.parser.errors :as err]
            [nanoweave.ast.primatives]))

(defn resolve-ast
  "Takes a parsed AST tree and transforms a given input with it"
  [ast input]
  (ast/safe-resolve-value ast input))

(defn parse-nweave-definition [nweave-definition]
  "Takes a string with an nanoweave definition and parses it to an AST tree"
  (kern/parse def/expr nweave-definition))

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
       ((println (err/format-error pstate))
         nil)))))

(defn transform-files
  "Transforms text from an input file with a given nanoweave
  defnition and writes the result to the output file"
  ([input-file output-file nweave-file]
   (let [input (read-json-with-doubles (slurp input-file))
         nweave (slurp nweave-file)
         output (json/write-str (transform input nweave))]
     (spit output-file output))))
