(ns jweave.parser.parser
  (:use [clojure.walk :only [postwalk]])
  (:require [blancas.kern.core :as kern]
            [clojure.data.json :as json]
            [jweave.parser.ast :as ast]
            [jweave.parser.definitions :as def]))

(defn resolve-ast
  [ast input]
  (postwalk #(if (satisfies? ast/Resolvable %) (ast/resolve-value % input) %) ast))

(defn transform
  [input jweave]
  (let [ast (kern/value def/jvalue jweave)
        result (resolve-ast ast input)]
    result))

(defn transform-files
  [input-file output-file jweave-file]
  (let [input (json/read-str (slurp input-file))
        jweave (slurp jweave-file)
        output (json/write-str (transform input jweave))]
    (spit output-file output)))
