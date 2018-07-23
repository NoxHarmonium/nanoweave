(ns nanoweave.core-test
  (:require [clojure.test :refer :all]
            [nanoweave.core :refer :all]
            [clojure.data :refer [diff]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]
            [clojure.walk :refer [prewalk]]
            [nanoweave.utils :refer [read-json-with-doubles]]))

(defn run-test-fixture [test-folder]
  (let [input-file (io/resource (str "test-fixtures/" test-folder "/input.json"))
        expected-file (io/resource (str "test-fixtures/" test-folder "/output.json"))
        nweave-file (io/resource (str "test-fixtures/" test-folder "/transform.nweave"))
        input (read-json-with-doubles (slurp input-file))
        expected (read-json-with-doubles (slurp expected-file))
        nweave (slurp nweave-file)
        actual (nanoweave.parser.parser/transform input nweave)]
    (println "Running test fixture: " test-folder)
    (is (= expected actual))))

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
    (run-test-fixture "map-collection")))
