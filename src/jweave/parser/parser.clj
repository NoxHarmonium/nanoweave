(ns jweave.parser.parser
  (:use blancas.kern.core
        clojure.pprint
        jweave.parser.definitions
        clojure.walk
        jweave.parser.ast)
  (:require [clojure.data.json :as json]))

(defn resolve-ast
  [ast input]
  (postwalk #(if (satisfies? Resolvable %) (resolve % input) %) ast))

(defn transform
  [input jweave]
  (let [ast (value jvalue jweave)
        result (resolve-ast ast input)]
    result))

(defn transform-files
  [input-file output-file jweave-file]
  (let [input (json/read-str (slurp input-file))
        jweave (slurp jweave-file)
        output (json/write-str (transform input jweave))]
    (spit output-file output)))
