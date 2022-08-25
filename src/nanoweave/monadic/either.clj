(ns ^{:doc "Represents either a success value or an error value.", :author "Sean Dawson"}
 nanoweave.monadic.either
  (:require [schema.core :as s]
            [nanoweave.monadic.base :refer [defmonad]]))

;; TODO: Can we use conditional or an error schema or something to clean this up?
(s/defrecord Either [ok :- s/Bool error :- s/Any value :- s/Any])

(defn ok? [e] (:ok e))

(defn ok
  "Wraps a value in an OK (right) type."
  [value]
  (->Either true nil value))
(defn err
  "Wraps a value in an Err (left) type."
  [error]
  (->Either false error nil))
(defn map-error
  "If e is an Err, apply function f to the wrapped error, otherwise pass through unchanged"
  [f e]
  (if (:ok e)
    e
    (err (f (:error e)))))
(defn return [v]
  (ok v))
(defn >>= [this f]
  (if (:ok this)
    (f (:value this))
    this))

#_{:clj-kondo/ignore [:unused-binding]}
(defmonad either-m
  "Monad describing computations with possible failures. Failure is
    represented by a 'left' value, a 'right' value is considered valid. As soon as
    a step returns a eft, the whole computation will yield the left value."
  [m-return return
   m-bind >>=])
