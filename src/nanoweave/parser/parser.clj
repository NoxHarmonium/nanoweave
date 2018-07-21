(ns nanoweave.parser.parser
  (:use [clojure.walk :only [postwalk]]
        [clojure.pprint])
  (:require [blancas.kern.core :as kern]
            [clojure.data.json :as json]
            [nanoweave.parser.ast :as ast]
            [nanoweave.parser.definitions :as def]))

(defn resolve-ast
  [ast input]
  (postwalk #(ast/safe-resolve-value % input) ast))

(defn transform
  [input nweave]
  (let [pstate (kern/parse def/expr nweave)
        ast (:value pstate)]
    (resolve-ast ast {"input" input})))

(defn transform-files
  [input-file output-file nweave-file]
  (let [input (json/read-str (slurp input-file))
        nweave (slurp nweave-file)
        output (json/write-str (transform input nweave))]
    (spit output-file output)))
