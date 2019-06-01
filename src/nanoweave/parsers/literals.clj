(ns ^{:doc "Pareses literal values."
      :author "Sean Dawson"}
 nanoweave.parsers.literals
  (:require [blancas.kern.core :refer [bind <?> return]]
            [blancas.kern.lexer.java-style :refer
             [identifier float-lit bool-lit nil-lit]]
            [nanoweave.ast.literals :refer [->IdentiferLit ->FloatLit ->BoolLit ->NilLit]]))

; Wrapped Primitives

(def wrapped-identifier
  (<?> (bind [v identifier]
             (return (->IdentiferLit v)))
       "identifier"))
(def wrapped-float-lit
  "Wraps a float-lit parser so it returns an AST record rather than a float."
  (<?> (bind [v float-lit]
             (return (->FloatLit (double v))))
       "float"))
(def wrapped-bool-lit
  "Wraps a bool-lit parser so it returns an AST record rather than a bool."
  (<?> (bind [v bool-lit] (return (->BoolLit v)))
       "boolean"))
(def wrapped-nil-lit
  "Wraps an nil-lit parser so it returns an AST record rather than a null."
  (<?> (bind [_ nil-lit] (return (->NilLit)))
       "null"))
