(ns nanoweave.java-interop
  (:require [clojure.reflect :as r]))

; JVM Reflection Wrappers

(defn call-static-method [^Class target ^String key #^Object marshalled-args]
  (clojure.lang.Reflector/invokeStaticMethod
   target key (object-array marshalled-args)))

; TODO: Catch errors like 'java.lang.IllegalArgumentException: No matching method getTimeZone found taking 1 args for class java.lang.Class'
; and suggest the usage of the static prefix (e.g. TimeZone.$getTimeZone ("GMT"))
(defn call-instance-method [^Object target ^String key #^Object marshalled-args]
  (clojure.lang.Reflector/invokeInstanceMethod
   target key (object-array marshalled-args)))

(defn get-static-field [^Class target ^String key]
  (clojure.lang.Reflector/getStaticField target key))

(defn get-instance-field [^String target ^String key]
  (clojure.lang.Reflector/getInstanceField target key))

; Clojure Native Calls

(defn members-matching-name [instance key]
  (filter #(= (name (:name %)) key) (:members (r/reflect instance :ancestors true))))

(defn matches-reflect-type? [instance key reflect-type is-static]
  ; If class, use std java reflection - otherwise do the thing
  (let [target (if (and (not is-static) (instance? java.lang.Class instance)) java.lang.Class instance)
        matching-members (members-matching-name target key)]
    (seq (filter (fn* [member] (instance? reflect-type member)) matching-members))))

(defn marshal-arg [arg]
  (cond
    (or (seq? arg) (vector? arg)) (to-array arg)
    :else arg))

(defn marshal-return [val]
  (cond
    (number? val) (double val)
    :else val))

(defn wrap-java-fn [target key is-static]
  (fn [_input & args]
    (marshal-return
     (let [marshalled-args (map marshal-arg args)]
       (if is-static
         (call-static-method target key (object-array marshalled-args))
         (call-instance-method target key (object-array marshalled-args)))))))

(defn call-java-constructor [class args]
  (clojure.lang.Reflector/invokeConstructor
   class (object-array args)))

(defn get-java-field [target key is-static]
  (if is-static
    (get-static-field target key)
    (get-instance-field target key)))

(defn byte-value
  "Calls the Java native byteValue() function on a number"
  [^java.lang.Number target]
  (.byteValue target))
(defn double-value
  "Calls the Java native doubleValue() function on a number"
  [^java.lang.Number target]
  (.doubleValue target))
(defn float-value
  "Calls the Java native floatValue() function on a number"
  [^java.lang.Number target]
  (.floatValue target))
(defn int-value
  "Calls the Java native intValue() function on a number"
  [^java.lang.Number target]
  (.intValue target))
(defn long-value
  "Calls the Java native longValue() function on a number"
  [^java.lang.Number target]
  (.longValue target))
(defn short-value
  "Calls the Java native shortValue() function on a number"
  [^java.lang.Number target]
  (.shortValue target))
(defn java-cast
  "Calls the Java native cast function for a given class"
  [^java.lang.Class clazz ^java.lang.Object target]
  (.cast clazz target))