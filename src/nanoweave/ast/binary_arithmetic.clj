(ns nanoweave.ast.binary-arithmetic
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord AddOp [left :- Resolvable right :- Resolvable])
(s/defrecord SubOp [left :- Resolvable right :- Resolvable])
(s/defrecord MultOp [left :- Resolvable right :- Resolvable])
(s/defrecord DivOp [left :- Resolvable right :- Resolvable])
(s/defrecord ModOp [left :- Resolvable right :- Resolvable])

(extend-protocol Resolvable
  AddOp
  (resolve-value [this input] (handle-bin-op this input +))
  SubOp
  (resolve-value [this input] (handle-bin-op this input -))
  MultOp
  (resolve-value [this input] (handle-bin-op this input *))
  DivOp
  (resolve-value [this input] (handle-bin-op this input /))
  ModOp
  (resolve-value [this input] (handle-bin-op this input mod)))