(ns nanoweave.parser.parser
  (:use [clojure.walk :only [prewalk]]
        [clojure.pprint])
  (:require [blancas.kern.core :as kern]
            [clojure.data.json :as json]
            [nanoweave.utils :refer [read-json-with-doubles]]
            [nanoweave.ast.base :as ast]
            [nanoweave.parser.definitions :as def]
            [nanoweave.parser.errors :as err]))

(defn resolve-ast
  [ast input]
  (prewalk #(ast/safe-resolve-value % input) ast))

(defn transform
  [input nweave]
  (let [pstate (kern/parse def/expr nweave)]
    (if (:ok pstate)
      (let [ast (:value pstate)]
        (resolve-ast ast {"input" input}))
      ((pprint (err/format-error pstate))
        nil))))


(defn transform-files
  [input-file output-file nweave-file]
  (let [input (read-json-with-doubles (slurp input-file))
        nweave (slurp nweave-file)
        output (json/write-str (transform input nweave))]
    (spit output-file output)))
