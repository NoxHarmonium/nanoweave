(ns nanoweave.core-test
  (:require [clojure.test :refer :all]
            [nanoweave.core :refer :all]
            [clojure.data :refer [diff]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]))

(deftest simple-structure-transform
  (testing "Can perform simple structure transform."
    (let [input-file (io/resource "test-fixtures/simple-structure-transform/input.json")
          expected-file (io/resource "test-fixtures/simple-structure-transform/output.json")
          nweave-file (io/resource "test-fixtures/simple-structure-transform/transform.nweave")
          input (json/read-str (slurp input-file))
          expected (json/read-str (slurp expected-file))
          nweave (slurp nweave-file)
          actual (nanoweave.parser.parser/transform input nweave)]
    (is (= expected actual)))))
