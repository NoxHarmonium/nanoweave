(ns jweave.core-test
  (:require [clojure.test :refer :all]
            [jweave.core :refer :all]
            [clojure.data :refer [diff]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]))

(deftest simple-structure-transform
  (testing "Can perform simple structure transform."
    (def input-file (io/resource "test-fixtures/simple-structure-transform/input.json"))
    (def expected-file (io/resource "test-fixtures/simple-structure-transform/output.json"))
    (def jweave-file (io/resource "test-fixtures/simple-structure-transform/transform.jweave"))
    (def input (json/read-str (slurp input-file)))
    (def expected (json/read-str (slurp expected-file)))
    (def jweave (slurp jweave-file))
    (def actual (jweave.parser.parser/transform input jweave))
    (is (= expected actual))
    ))
