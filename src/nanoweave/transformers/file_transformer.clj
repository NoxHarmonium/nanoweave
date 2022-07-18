(ns ^{:doc "The transformer for nanoweave.", :author "Sean Dawson"}
 nanoweave.transformers.file-transformer
  (:require [blancas.kern.core :refer [parse]]
            [cheshire.core :refer [generate-string]]
            [nanoweave.utils :refer [read-json-with-doubles]]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.parsers.expr :refer [single-expression]]
            [nanoweave.parsers.errors :refer [format-parsing-error-as-code-frame]]
            [nanoweave.resolvers.errors :refer [format-resolution-error-as-code-frame]]
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
            [nanoweave.resolvers.unary]))

(defn resolve-ast
  "Takes a parsed AST tree and transforms a given input with it"
  [ast input nweave]
  (try (safe-resolve-value ast input)
       (catch clojure.lang.ExceptionInfo e
         (throw (if-let
                 [ast-node (:ast-node (ex-data e))]
                  (ex-info (format-resolution-error-as-code-frame
                            ast-node (ex-message e) nweave) {:ast-node ast-node})
                  e)))))

(defn parse-nweave-definition
  "Takes a string with an nanoweave definition and parses it to an AST tree"
  [nweave-definition filename]
  (parse single-expression nweave-definition filename))

(defn transform
  "Transforms input with the given nanoweave definition and an optional
  transform function."
  ([input nweave filename]
   (transform input nweave filename resolve-ast))
  ([input nweave filename transform-fn]
   (let [pstate (parse-nweave-definition nweave filename)]
     (if (:ok pstate)
       (let [ast (:value pstate)]
         ;; Resolve errors currently throw because it would take a pretty big refactor to
         ;; pass errors back out (probably would have to (ab)use something like the kern
         ;; state monad thing)
         (transform-fn ast {"input" input} nweave))
       (throw (ex-info (format-parsing-error-as-code-frame pstate nweave) {:pstate pstate}))))))

(defn transform-files
  "Transforms text from an input file with a given nanoweave
  defnition and writes the result to the output file"
  ([input-file output-file nweave-file]
   (let [input (read-json-with-doubles (slurp input-file))
         nweave (slurp nweave-file)
         output (generate-string (transform input nweave nweave-file))]
     (spit output-file output))))
