(ns nanoweave.java-interop
  (:require [clojure.reflect :as r]))

; JVM Reflection Wrappers

(defn call-static-method [^Class target ^String key #^Object marshalled-args]
  (clojure.lang.Reflector/invokeStaticMethod
   target key (object-array marshalled-args)))

(defn call-instance-method [^Object target ^String key #^Object marshalled-args]
  (clojure.lang.Reflector/invokeInstanceMethod
   target key (object-array marshalled-args)))

(defn get-static-field [^Class target ^String key]
  (clojure.lang.Reflector/getStaticField target key))

(defn get-instance-field [^String target ^String key]
  (clojure.lang.Reflector/getInstanceField target key))

; Clojure Native Calls

(defn members-matching-name [instance key]
  (filter #(= (name (:name %)) key) (:members (r/reflect instance))))

(defn matches-reflect-type? [instance key reflect-type]
  (let [matching-members (members-matching-name instance key)]
    (seq (filter (fn* [p1__281412#] (instance? reflect-type p1__281412#)) matching-members))))

(defn marshal-arg [arg]
  (cond
    (or (seq? arg) (vector? arg)) (to-array arg)
    :else arg))

(defn marshal-return [val]
  (cond
    (number? val) (double val)
    :else val))

(defn wrap-java-fn [target key]
  (fn [_input & args]
    (marshal-return
     (let [marshalled-args (map marshal-arg args)]
       (if (class? target)
         (call-static-method target key (object-array marshalled-args))
         (call-instance-method target key (object-array marshalled-args)))))))

(defn call-java-constructor [class args]
  (clojure.lang.Reflector/invokeConstructor
   class (object-array args)))

(defn get-java-field [target key]
  (if (class? target)
    (get-static-field target key)
    (get-instance-field target key)))
