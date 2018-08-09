(ns nanoweave.core-test
  (:require [clojure.test :refer :all]
            [nanoweave.core :refer :all]
            [clojure.data :refer [diff]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]
            [clojure.walk :refer [prewalk]]
            [nanoweave.utils :refer [read-json-with-doubles]]
            [nanoweave.parser.parser :as parser]))

(defn run-test-fixture [test-folder]
  (println "Running test fixture: " test-folder)
  (let [input-file (io/resource (str "test-fixtures/" test-folder "/input.json"))
        expected-file (io/resource (str "test-fixtures/" test-folder "/output.json"))
        nweave-file (io/resource (str "test-fixtures/" test-folder "/transform.nweave"))
        input (read-json-with-doubles (slurp input-file))
        expected (read-json-with-doubles (slurp expected-file))
        nweave (slurp nweave-file)
        actual (parser/transform input nweave parser/resolve-ast)]
    (is (= expected actual))))

; Future work: work out how to dynamically create tests based on test-fixtures directory
; A simple loop didn't work, I'll probably need a macro
(deftest io-tests
  (testing "Structure Transformation"
    (run-test-fixture "simple-structure-transform"))
  (testing "String concatination"
    (run-test-fixture "concat-operator"))
  (testing "arithmetic"
    (run-test-fixture "basic-arithmetic"))
  (testing "Boolean Logic"
    (run-test-fixture "boolean-logic"))
  (testing "Map Collection"
    (run-test-fixture "map-collection"))
  (testing "Basic Functional Operators"
    (run-test-fixture "basic-functional-operators"))
  (testing "Basic Variables"
    (run-test-fixture "basic-variables"))
  (testing "String Interpolation"
    (run-test-fixture "string-interpolation"))
  (testing "Function Calling"
    (run-test-fixture "function-calling"))
  (testing "Indexing"
    (run-test-fixture "indexing"))
  (testing "Ranges"
    (run-test-fixture "ranges")))
