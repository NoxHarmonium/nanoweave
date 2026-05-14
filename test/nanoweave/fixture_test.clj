(ns nanoweave.fixture-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.java.io :as io]
            [clojure.walk :refer [postwalk]]
            [nanoweave.io-utils :refer [read-json-with-doubles]]
            [nanoweave.transformers.file-transformer :as transformer]
            [diff-eq.core :as de]
            [schema.core :as s]
            [schema.utils :refer [class-schema]]
            [schema.test]))

;; Patch the eq function to provide diffs for object comparisons

(de/diff!)

(use-fixtures :once schema.test/validate-schemas)

(defn- validate-ast-tree [ast]
  (postwalk
   (fn [node]
     (when (record? node)
       (when-let [schema (class-schema (class node))]
         (s/validate schema node)))
     node)
   ast))

(defn validate-transform-output [test-folder]
  (println "==> Running test fixture transform and checking output: " test-folder)
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

(defn validate-fixture-ast-types [test-folder]
  (println "==> Validating types for test fixture: " test-folder)
  (let [nweave-filename (str "test-fixtures/" test-folder "/transform.nweave")
        nweave-file (io/resource nweave-filename)
        nweave (slurp nweave-file)
        pstate (transformer/parse-nweave-definition nweave nweave-filename)
        ast (:value pstate)]
    (is (= (:ok pstate) true))
    (is (validate-ast-tree ast))))

(defn run-test-fixture [test-folder]
  (println "Running test fixture: " test-folder)
  ; Run this first because it gives much nicer error messages if parser fails
  (validate-transform-output test-folder)
  (validate-fixture-ast-types test-folder))

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
