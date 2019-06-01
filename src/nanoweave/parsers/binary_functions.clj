(ns ^{:doc "Parses functional programming style operations
            that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.parsers.binary-functions
  (:require [blancas.kern.core :refer [bind <|> <?> return]]
            [blancas.kern.lexer.java-style :refer [token]]
            [nanoweave.ast.binary-functions :refer
             [->MapOp ->FilterOp ->ReduceOp]]))

; Functional Binary Operators

(def map-op
  "Map sequence operator"
  (<?> (bind [_ (token "map")]
             (return ->MapOp))
       "map operator"))
(def filter-op
  "Filter sequence operator"
  (<?> (bind [_ (token "filter")]
             (return ->FilterOp))
       "filter operator"))
(def reduce-op
  "Reduce sequence operator"
  (<?> (bind [_ (token "reduce")]
             (return ->ReduceOp))
       "reduce operator"))
