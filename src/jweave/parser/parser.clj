(ns jweave.parser.parser
  (:use blancas.kern.core
        clojure.pprint
        jweave.parser.definitions
        clojure.walk
        jweave.parser.ast)
  (:require [clojure.data.json :as json]))

; Test with
; lein run -i test\resources\test-fixtures\simple-structure-transform\input.json -o output.json -j test\resources\test-fixtures\simple-structure-transform\transform.jweave transform

(defn resolve-ast [ast input]
  (postwalk
    #(if (satisfies? Resolvable %) (resolve % input) %)
    ast))

(defn transform [input-file, output-file, jweave-file]
  (def input (json/read-str (slurp input-file)))
  (def ast (value jvalue (slurp jweave-file)))
  (def result (resolve-ast ast input))
  (pprint result)
  (spit output-file (json/write-str result)))
