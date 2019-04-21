(ns nanoweave.ast.literals
  (:require [schema.core :as s]
            [clojure.reflect :as r])
  (:use nanoweave.ast.base))

(s/defrecord IdentiferLit [value :- s/Str])
(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Num])
(s/defrecord BoolLit [value :- s/Bool])
(s/defrecord NilLit [])
(s/defrecord ArrayLit [value :- [Resolvable]])

(defn members-matching-name [instance key]
  (filter #(= (name (:name %)) key) (:members (r/reflect instance))))

(defn matches-reflect-type? [instance key reflect-type]
  (let [matching-members (members-matching-name instance key)]
    (not (empty? (filter #(instance? reflect-type %) matching-members)))))

(defn wrap-java-fn [instance key]
  (fn [& args]
    (clojure.lang.Reflector/invokeInstanceMethod
     instance key (object-array args))))

(defn wrap-java-constructor [class]
  (fn [& args]
    (clojure.lang.Reflector/invokeConstructor
     class (object-array args))))

(defn get-java-field [instance key]
  (clojure.lang.Reflector/getInstanceField instance key))

(extend-protocol Resolvable
  IdentiferLit
  (resolve-value [this input]
    (let [key (:value this)]
      (cond
        (map? input) (get input key)
        (matches-reflect-type? input key clojure.reflect.Method) (wrap-java-fn input key)
        (matches-reflect-type? input key clojure.reflect.Field) (get-java-field input key)
        :else (throw (Exception. (str "Not sure how to resolve key [" key "] on [" (type input) "]"))))))
  StringLit
  (resolve-value [this _] (:value this))
  FloatLit
  (resolve-value [this _] (:value this))
  BoolLit
  (resolve-value [this _] (:value this))
  NilLit
  (resolve-value [_ _] nil)
  ArrayLit
  (resolve-value [this input]
    (map #(safe-resolve-value %1 input) (:value this))))