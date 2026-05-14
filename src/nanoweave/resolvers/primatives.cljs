(ns nanoweave.resolvers.primatives
  (:require [nanoweave.resolvers.base :refer [safe-resolve-value]]
            [nanoweave.utils :refer [map-vals]]
            [nanoweave.ast.base :refer [Resolvable]]))

(extend-protocol Resolvable
  string
  (resolve-value [this _] this)
  number
  (resolve-value [this _] this)
  boolean
  (resolve-value [this _] this)
  cljs.core/PersistentArrayMap
  (resolve-value [this input]
    (map-vals #(safe-resolve-value % input) this))
  cljs.core/PersistentHashMap
  (resolve-value [this input]
    (map-vals #(safe-resolve-value % input) this))
  cljs.core/PersistentVector
  (resolve-value [this input]
    (map #(safe-resolve-value % input) this)))
