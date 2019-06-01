(ns ^{:doc "Syntax that represents miscellaneous operations that can be done on a single expression."
      :author "Sean Dawson"}
 nanoweave.ast.unary
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord NotOp [value :- Resolvable])
(s/defrecord NegOp [value :- Resolvable])
