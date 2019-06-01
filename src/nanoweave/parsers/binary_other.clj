(ns ^{:doc "Other binary parsers", :author "Sean Dawson"}
 nanoweave.parsers.binary-other
  (:require [blancas.kern.core :refer [bind <?> return]]
            [blancas.kern.lexer.java-style :refer
              [dot token]]
            [nanoweave.ast.binary-other :refer
              [->DotOp ->ConcatOp ->OpenRangeOp ->ClosedRangeOp]]))

; Other Binary Operators

(def dot-op
  "Access operator: extract value from object."
  (<?> (bind [_ dot]
             (return ->DotOp))
       "property accessor (.)"))
(def concat-op
  "Parses one of the relational operators."
  (<?> (bind [_ (token "++")]
             (return ->ConcatOp))
       "concat operator (++)"))
(def open-range-op
  "Parses an open range expression."
  (<?> (bind [_ (token "until")]
             (return ->OpenRangeOp))
       "open range expression (until)"))
(def closed-range-op
  "Parses an closed range expression."
  (<?> (bind [_ (token "to")]
             (return ->ClosedRangeOp))
       "closed range expression (to)"))