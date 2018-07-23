(ns nanoweave.ast.primatives
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(extend-protocol Resolvable
  String
  (resolve-value [this _] this)
  Double
  (resolve-value [this _] this))
