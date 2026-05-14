(ns nanoweave.fixture-cljs-test
  (:require [cljs.test :refer [deftest is testing]]
            [clojure.walk :refer [postwalk]]
            [nanoweave.io-utils :refer [read-json-with-doubles]]
            [nanoweave.transformers.string-transformer :refer [transform-strings parse-nweave-definition]]
            [schema.core :as s]
            [schema.utils :refer [record? class-schema]]))

(defn- read-file [path]
  (let [fs (js/require "fs")]
    (.readFileSync fs path "utf8")))

(defn- fixture-path [folder file]
  (str "test/resources/test-fixtures/" folder "/" file))

(defn- read-expected [test-folder]
  (let [fs (js/require "fs")
        cljs-path (fixture-path test-folder "output.cljs.json")
        default-path (fixture-path test-folder "output.json")]
    (read-json-with-doubles
     (if (.existsSync fs cljs-path)
       (read-file cljs-path)
       (read-file default-path)))))

(defn- validate-ast-tree [ast]
  (postwalk
   (fn [node]
     (when (record? node)
       (when-let [schema (class-schema (type node))]
         (s/validate schema node)))
     node)
   ast))

(defn run-test-fixture [test-folder]
  (println "Running CLJS test fixture: " test-folder)
  (let [input-str (read-file (fixture-path test-folder "input.json"))
        expected (read-expected test-folder)
        nweave-str (read-file (fixture-path test-folder "transform.nweave"))
        actual (transform-strings input-str nweave-str)]
    (is (= (:ok actual) true) (str test-folder " - expected ok result"))
    (is (= expected (:value actual)) (str test-folder " - value mismatch")))
  (let [nweave-str (read-file (fixture-path test-folder "transform.nweave"))
        pstate (parse-nweave-definition nweave-str (str "test-fixtures/" test-folder "/transform.nweave"))]
    (is (= (:ok pstate) true) (str test-folder " - expected parse ok"))
    (is (validate-ast-tree (:value pstate)) (str test-folder " - ast validation"))))

(deftest fixture-test
  (testing "Structure Transformation"
    (run-test-fixture "simple-structure-transform"))
  (testing "String concatenation"
    (run-test-fixture "concat-operator"))
  (testing "Arithmetic"
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
  (testing "Pattern Matching"
    (run-test-fixture "pattern-matching"))
  (testing "Flow Control"
    (run-test-fixture "flow-control"))
  (testing "Regex"
    (run-test-fixture "regex")))
  ;; type-checking-coercion intentionally excluded — Java class imports not available in CLJS
  ;; java-interop fixture intentionally excluded — Java reflection not available in CLJS
