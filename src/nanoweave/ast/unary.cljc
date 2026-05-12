(ns ^{:doc "Syntax that represents miscellaneous operations that can be done on a single expression."
      :author "Sean Dawson"}
 nanoweave.ast.unary
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord NotOp [span :- AstSpan value :- Resolvable])
(s/defrecord NegOp [span :- AstSpan value :- Resolvable])
(s/defrecord TypeOfOp [span :- AstSpan value :- Resolvable])
