(ns nanoweave.parser.parser
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
  [ast input]
  (ast/safe-resolve-value ast input))

(defn parse-nweave-definition [nweave-definition]
  (kern/parse def/expr nweave-definition))

(defn transform
  [input nweave transform-fn]
  (let [pstate (parse-nweave-definition nweave)]
    (if (:ok pstate)
      (let [ast (:value pstate)]
        (transform-fn ast {"input" input}))
      ((println (err/format-error pstate))
        nil))))

(defn transform-files
  ([input-file output-file nweave-file]
   (let [input (read-json-with-doubles (slurp input-file))
         nweave (slurp nweave-file)
         output (json/write-str (transform input nweave resolve-ast))]
     (spit output-file output))))
