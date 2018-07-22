(ns nanoweave.parser.parser
  (:use [clojure.walk :only [postwalk]]
        [clojure.pprint])
  (:require [blancas.kern.core :as kern]
            [blancas.kern.i18n :refer [i18n]]
            [clojure.data.json :as json]
            [clojure.string :refer [join]]
            [nanoweave.parser.ast :as ast]
            [nanoweave.parser.definitions :as def]
            [nanoweave.parser.errors :as err]))

(defn resolve-ast
  [ast input]
  (postwalk #(ast/safe-resolve-value % input) ast))



(defn transform
  [input nweave]
  (let [pstate (kern/parse def/expr nweave)]
    (if (:ok pstate)
      (resolve-ast (:value pstate) {"input" input})
      (err/format-error pstate))
    ))

(defn transform-files
  [input-file output-file nweave-file]
  (let [input (json/read-str (slurp input-file))
        nweave (slurp nweave-file)
        output (json/write-str (transform input nweave))]
    (spit output-file output)))
