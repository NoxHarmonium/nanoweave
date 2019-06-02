(ns nanoweave.resolvers.lambda
  (:require [nanoweave.ast.lambda]
            [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.java-interop :as j])
  (:import [nanoweave.ast.lambda Lambda NoArgsLambda FunCall ArgList])
  (:use [nanoweave.ast.base :only [resolve-value Resolvable]]))

(defn- check-param-count
  "Checks that the expected number of arguments are passed to a function"
  [args param-list]
  (let [args-count (count args) param-list-count (count param-list)]
    (assert (= (count args) (count param-list))
            (str "incorrect number of params passed to lambda.
            Expected " param-list-count " Got " args-count
                 " (" (map type args) ")"))))

(defn- generate-params
  "For lambdas that don't have arguments specified,
  this generates the parameters used as arguments."
  [count]
  (map #(str "%" (inc %1)) (range count)))

(extend-protocol Resolvable
  Lambda
  (resolve-value [this input]
    (let [param-list (:param-list this)
          body (:body this)]
      (fn [_input & args]
        (check-param-count args param-list)
        (let [merged-input (merge
                            input
                            _input
                            (zipmap param-list args))]
          (safe-resolve-value body merged-input)))))
  NoArgsLambda
  (resolve-value [this input]
    (let [body (:body this)]
      (fn [_input & args]
        (let [param-list (generate-params (count args))]
          (safe-resolve-value body (merge
                                    input
                                    _input
                                    (zipmap param-list args)))))))
  FunCall
  (resolve-value [this input]
    (let [target (safe-resolve-value (:target this) input)
          args (safe-resolve-value (:args this) input)
          resolved-args (get args :resolved-arguments [args])]
      (cond
        (fn? target) (apply target (cons input resolved-args))
        (instance? java.lang.Class target) (j/call-java-constructor target resolved-args)
        :else (throw (Exception. (str "Not sure how to call [" target "(" (type target) ")]"))))))
  ArgList
  (resolve-value [this input]
    (let [arguments (:arguments this)]
      {:resolved-arguments (map #(safe-resolve-value % input) arguments)})))
