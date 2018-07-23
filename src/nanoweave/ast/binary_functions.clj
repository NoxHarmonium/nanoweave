(ns nanoweave.ast.binary-functions
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord MapOp [left :- Resolvable right :- Resolvable])

(extend-protocol Resolvable
  MapOp
  (resolve-value [this input] (handle-bin-op this input #(map %2 %1))))

