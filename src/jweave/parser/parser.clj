(ns jweave.parser.parser
  (:use blancas.kern.core
        clojure.pprint
        jweave.parser.definitions
        clojure.algo.generic.functor))

(defn run-ast [ast]
  fmap (fn [val] fn? val) ast)

(defn transform [input-file, output-file, jweave-file]
  (def ast (run jvalue (slurp jweave-file)))
  (def result (run-ast ast))
  (pprint result))