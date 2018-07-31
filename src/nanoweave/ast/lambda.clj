(ns nanoweave.ast.lambda
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord Lambda [param-list :- [s/Str] body :- Resolvable])
(s/defrecord NoArgsLambda [body :- Resolvable])
(s/defrecord FunCall [target :- Resolvable args :- [Resolvable]])

(defn- check-param-count [args, param-list]
  (let [args-count (count args) param-list-count (count param-list)]
    (assert (= (count args) (count param-list))
            (str "incorrect number of params passed to lambda.
            Expected " param-list-count " Got " args-count
                 " (" (map type args) ")"))))

(defn- generate-params [count]
  "For lambdas that don't have arguments specified,
  this generates the parameters used as arguments."
  (map #(str "%" (+ %1 1)) (range count)))

(extend-protocol Resolvable
  Lambda
  (resolve-value [this input]
    (let [param-list (:param-list this)
          body (:body this)]
      (fn [& args]
        (check-param-count args param-list)
        (safe-resolve-value body (merge
                                   input
                                   (zipmap param-list args))))))
  NoArgsLambda
  (resolve-value [this input]
    (let [body (:body this)]
      (fn [& args]
        (let [param-list (generate-params (count args))]
          (safe-resolve-value body (merge
                                     input
                                     (zipmap param-list args)))))))
  FunCall
  (resolve-value [this input]
    (let [target (safe-resolve-value (:target this) input)
          args (safe-resolve-value (:args this) input)]
      (apply target args))))
