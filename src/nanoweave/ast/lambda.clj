(ns nanoweave.ast.lambda
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord Lambda [param-list :- [s/Str] body :- Resolvable])

(defn- check-param-count [args, param-list]
  (let [args-count (count args) param-list-count (count param-list)]
    (assert (= (count args) (count param-list))
            (str "incorrect number of params passed to lambda.
            Expected " param-list-count " Got " args-count
            " (" (map type args) ")"))))

(extend-protocol Resolvable
  Lambda
  (resolve-value [this input]
    (let [param-list (:param-list this)
          body (:body this)]
      (fn [& args]
        (check-param-count args param-list)
        (safe-resolve-value body (merge
                                   input
                                   (zipmap param-list args)))))))
