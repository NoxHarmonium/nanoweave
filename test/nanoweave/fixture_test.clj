(ns nanoweave.fixture-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [nanoweave.utils :refer [read-json-with-doubles]]
            [nanoweave.transformers.file-transformer :as transformer]
            [diff-eq.core :as de]))

;; Patch the eq function to provide diffs for object comparisons

(de/diff!)

(defn run-test-fixture [test-folder]
  (println "Running test fixture: " test-folder)
  (let [input-file (io/resource (str "test-fixtures/" test-folder "/input.json"))
        expected-file (io/resource (str "test-fixtures/" test-folder "/output.json"))
        nweave-filename (str "test-fixtures/" test-folder "/transform.nweave")
        nweave-file (io/resource nweave-filename)
        input (read-json-with-doubles (slurp input-file))
        expected (read-json-with-doubles (slurp expected-file))
        nweave (slurp nweave-file)
        actual (transformer/transform input nweave nweave-filename)]
    (is (= (:ok actual) true))
    (is (= expected (:value actual)))))

; Future work: work out how to dynamically create tests based on test-fixtures directory
; A simple loop didn't work, I'll probably need a macro
(deftest fixture-test
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
    (run-test-fixture "ranges"))
  (testing "Java Interop"
    (run-test-fixture "java-interop"))
  (testing "Pattern Matching"
    (run-test-fixture "pattern-matching"))
  (testing "Flow Control"
    (run-test-fixture "flow-control"))
  (testing "Regex"
    (run-test-fixture "regex"))
  (testing "Type Checking and Coercion"
    (run-test-fixture "type-checking-coercion")))
