(ns nanoweave.core-test
  (:require [clojure.test :refer :all]
            [nanoweave.core :refer :all]
            [clojure.data :refer [diff]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]))

(defn do-test [test-folder]
  (testing (str "Test fixture: " test-folder)
      (let [input-file (io/resource (str "test-fixtures/" test-folder "/input.json"))
            expected-file (io/resource (str "test-fixtures/" test-folder "/output.json"))
            nweave-file (io/resource (str "test-fixtures/" test-folder "/transform.nweave"))
            input (json/read-str (slurp input-file))
            expected (json/read-str (slurp expected-file))
            nweave (slurp nweave-file)
            actual (nanoweave.parser.parser/transform input nweave)]
        (is (= expected actual)))))

(deftest simple-structure-transform
  (do-test "simple-structure-transform")
  (do-test "concat-operator"))