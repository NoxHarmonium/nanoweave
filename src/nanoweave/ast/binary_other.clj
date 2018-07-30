(ns nanoweave.ast.binary-other
  (:require [schema.core :as s])
  (:use nanoweave.ast.base))

(s/defrecord DotOp [left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [left :- Resolvable right :- Resolvable])
(s/defrecord AssignOp [left :- Resolvable right :- Resolvable])

(extend-protocol Resolvable
  DotOp
  (resolve-value [this input] (handle-prop-access this input))
  ConcatOp
  (resolve-value [this input] (handle-bin-op this input str))
  AssignOp
  (resolve-value [this input] (handle-bin-op this input #({%1 %2}))))
