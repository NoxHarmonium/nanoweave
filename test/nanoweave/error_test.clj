(ns nanoweave.error-test
  (:require [clojure.test :refer [deftest is testing]]
            [nanoweave.parsers.errors :refer [format-parsing-error-as-code-frame]]
            [nanoweave.transformers.file-transformer :refer [parse-nweave-definition]]
            [clojure.string :refer [trim]]))

(deftest error-test
  (testing "Parsing errors single line"
    (let [pstate (parse-nweave-definition "{ h: \"a }" "(unit test)")
          error (:error pstate)
          pos (:pos error)]
      (is (= false (:ok pstate)))
      (is (= 1 (:line pos)))
      (is (= 10 (:col pos)))
      (is (= "(unit test)" (:src pos)))))
  (testing "Parsing errors multiple lines"
    (let [pstate (parse-nweave-definition "1 + \n\n\n\n 2 +++ \n\n\n\n\n\n3" "(unit test)")
          error (:error pstate)
          pos (:pos error)]
      (is (= false (:ok pstate)))
      (is (= 5 (:line pos)))
      (is (= 7 (:col pos)))
      (is (= "(unit test)" (:src pos)))))
  (testing "Formatting parsing errors"
    (let [pstate {:ok false :error {:pos {:line 3 :col 5 :src "(unit test)"}}}
          error-message (format-parsing-error-as-code-frame pstate (trim "
{
    a: 1 + 4,
    b: 1 + 4,
    c: 1 + 4 a b c,
    d: 1 + 4,
    e: 1 + 4,
}
    "))]
      (is (= (trim "Parsing error: (unit test) line 3 column 5
1| {
2|     a: 1 + 4,
3|     b: 1 + 4,
       ^ 
4|     c: 1 + 4 a b c,
5|     d: 1 + 4,") (trim error-message))))))