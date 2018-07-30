(ns nanoweave.ast.primatives
  (:use nanoweave.ast.base)
  (:require [nanoweave.utils :refer [map-vals]])
  (:import (clojure.lang PersistentArrayMap PersistentVector PersistentHashMap)))

(extend-protocol Resolvable
  String
  (resolve-value [this _] this)
  Double
  (resolve-value [this _] this)
  PersistentArrayMap
  (resolve-value [this input]
    (map-vals #(safe-resolve-value % input) this))
  PersistentHashMap
  (resolve-value [this input]
    (map-vals #(safe-resolve-value % input) this))
  PersistentVector
  (resolve-value [this input]
    (map #(safe-resolve-value % input) this)))
