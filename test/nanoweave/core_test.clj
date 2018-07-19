(ns nanoweave.core-test
  (:require [clojure.test :refer :all]
            [nanoweave.core :refer :all]
            [clojure.data :refer [diff]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]))

(deftest simple-structure-transform
  (testing "Can perform simple structure transform."
    (def input-file (io/resource "test-fixtures/simple-structure-transform/input.json"))
    (def expected-file (io/resource "test-fixtures/simple-structure-transform/output.json"))
    (def nweave-file (io/resource "test-fixtures/simple-structure-transform/transform.nweave"))
    (def input (json/read-str (slurp input-file)))
    (def expected (json/read-str (slurp expected-file)))
    (def nweave (slurp nweave-file))
    (def actual (nanoweave.parser.parser/transform input nweave))
    (is (= expected actual))
    ))
