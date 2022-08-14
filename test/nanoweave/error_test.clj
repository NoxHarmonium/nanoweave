(ns nanoweave.error-test
  (:require [clojure.test :refer [deftest is testing]]
            [nanoweave.ast.lambda :refer [->FunCall ->ArgList]]
            [nanoweave.ast.base :refer [->AstSpan ->AstPos ->ErrorWithContext]]
            [nanoweave.ast.literals :refer [->FloatLit]]
            [nanoweave.utils :refer [format-error-with-context]]
            [nanoweave.parsers.errors :refer [convert-pstate-to-error-with-context]]
            [nanoweave.transformers.file-transformer :refer [parse-nweave-definition resolve-ast]]
            [clojure.string :refer [trim]]))

(def bad-ast (->FunCall (->AstSpan
                         (->AstPos 1 1 "(unit test)"),
                         (->AstPos 1 2 "(unit test)")),
                        (->FloatLit
                         (->AstSpan
                          (->AstPos 1 1 "(unit test)")
                          (->AstPos 1 1 "(unit test)")) 2.0)
                        (->ArgList
                         (->AstSpan
                          (->AstPos 1 3 "(unit test)")
                          (->AstPos 1 2 "(unit test)")) [])))

(deftest parsing-errors
  (testing "Parsing errors single line"
    (let [pstate (parse-nweave-definition "{ h: \"a }" "(unit test)")
          contextual-error (convert-pstate-to-error-with-context pstate "{ h: \"a }")
          {:keys [start end]} (:span contextual-error)]
      (is (= false (:ok pstate)))
      (is (= start end)) ; Parsing errors are a single position, not a full span
      (is (= 1 (:line start)))
      (is (= 10 (:col start)))
      (is (= "(unit test)" (:src start)))))
  (testing "Parsing errors multiple lines"
    (let [pstate (parse-nweave-definition "1 + \n\n\n\n 2 +++ \n\n\n\n\n\n3" "(unit test)")
          contextual-error (convert-pstate-to-error-with-context pstate "1 + \n\n\n\n 2 +++ \n\n\n\n\n\n3")
          {:keys [start end]} (:span contextual-error)]
      (is (= false (:ok pstate)))
      (is (= start end)) ; Parsing errors are a single position, not a full span
      (is (= 5 (:line start)))
      (is (= 7 (:col start)))
      (is (= "(unit test)" (:src start)))))
  (testing "Formatting parsing errors"
    (let [nweave (trim "
{
    a: 1 + 4,
    b: 1 + 4,
    c: 1 + 4 a b c,
    d: 1 + 4,
    e: 1 + 4,
}
    ")
          contextual-error (->ErrorWithContext "expecting ," :parse-error nil (->AstSpan
                                                                               (->AstPos 4 14 "(unit test)"),
                                                                               (->AstPos 4 14 "(unit test)")) nil nweave)

          error-message (format-error-with-context contextual-error)]
      (is (= (trim "Compliation failure: (ln: 4 col: 14 src: (unit test))
2|     a: 1 + 4,
3|     b: 1 + 4,
4|     c: 1 + 4 a b c,
                ^ expecting ,
5|     d: 1 + 4,
6|     e: 1 + 4,") (trim error-message))))))

(deftest resolution-errors
  (testing "Resolution error (calling non function)"
    (let [result (resolve-ast bad-ast {})
          error (:error result)
          {:keys [start end]} (:span error)]
      (is (= false (:ok result)))
      (is (= 1 (:line start)))
      (is (= 1 (:col start)))
      (is (= "(unit test)" (:src start)))
      (is (= 1 (:line end)))
      (is (= 2 (:col end)))
      (is (= "(unit test)" (:src end)))))
  (testing "Formatting resolution errors"
    (let [result (resolve-ast bad-ast {})
          contextual-error (assoc (:error result) :input "2()")
          error-message (format-error-with-context contextual-error)]
      (is (= false (:ok result)))
      (is (= (trim error-message) (trim "
Compliation failure: (ln: 1 col: 1 src: (unit test))

1| 2()
   ~~
Not sure how to call [2.0] (class java.lang.Double)
"))))))