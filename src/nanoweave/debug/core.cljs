(ns nanoweave.debug.core
  (:require [nanoweave.transformers.string-transformer :refer [transform-strings]]
            [nanoweave.ast.lambda :refer [->ArgList]]
            [nanoweave.ast.literals :refer [->FloatLit]]
            [nanoweave.ast.base :refer [resolve-value]]
            [blancas.kern.core :refer [parse]]
            [nanoweave.parsers.expr :refer [single-expression]]))

(defn run [label input-str nweave-str]
  (let [result (transform-strings input-str nweave-str)]
    (println "==" label "==")
    (if (:ok result)
      (println "OK:" (pr-str (:value result)))
      (println "FAIL:" (:message (:error result))))))

(defn run [label input-str nweave-str]
  (let [result (transform-strings input-str nweave-str)]
    (println "==" label "==")
    (if (:ok result)
      (println "OK:" (pr-str (:value result)))
      (println "FAIL:" (:message (:error result))))))

(defn run-fixture [folder]
  (let [fs (js/require "fs")
        input  (.readFileSync fs (str "test/resources/test-fixtures/" folder "/input.json") "utf8")
        nweave (.readFileSync fs (str "test/resources/test-fixtures/" folder "/transform.nweave") "utf8")
        result (transform-strings input nweave)]
    (println "==" folder "==")
    (if (:ok result)
      (println "OK:" (pr-str (:value result)))
      (println "FAIL:" (:message (:error result))))))

(defn -main []
  (run-fixture "flow-control")
  (run-fixture "function-calling"))

(-main)
