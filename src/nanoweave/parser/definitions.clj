(ns ^{:doc "The nanoweave parser definitions.", :author "Sean Dawson"}
 nanoweave.parser.definitions
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.expr :refer :all]
            [blancas.kern.lexer.java-style :refer :all]
            [nanoweave.parser.custom-lexing :refer :all]
            [nanoweave.parsers.base :refer [pair array object]]
            [nanoweave.parsers.primatives :refer
              [wrapped-identifier wrapped-float-lit wrapped-bool-lit wrapped-nil-lit]]
            [nanoweave.parsers.unary :refer [wrapped-uni-op]]
            [nanoweave.parsers.binary-arithmetic :refer
              [wrapped-mul-op wrapped-add-op]]
            [nanoweave.parsers.binary-logic :refer
              [wrapped-rel-op wrapped-eq-op wrapped-and-op wrapped-or-op wrapped-xor-op]]
            [nanoweave.parsers.binary-functions :refer
              [map-op filter-op reduce-op]]
            [nanoweave.parsers.lambda :refer
              [argument-list lambda-body lambda no-args-lambda no-args-lambda-param
              function-call function-arguments]]
            [nanoweave.parsers.binary-other :refer
              [dot-op concat-op open-range-op closed-range-op]]
            [nanoweave.parsers.scope :refer
              [literal-match variable-match key-match key-value-match list-pattern-match
               map-pattern-match binding-target variable-binding binding-list let-scope
               when-clause when-scope else match-clause match-scope indexing import-statement
               wrapped-interpolated-string]]
            [nanoweave.ast.literals :refer [->IdentiferLit ->BoolLit]]))

; Forward declarations

(declare expr)

(def fun-ops
  "Matches any of the functional binary operators (they have the same precedence)"
  (<|> map-op filter-op reduce-op))

; Root Definition

(def nweave
  "Parses a nanoweave structure."
  (<|>
   let-scope
   when-scope
   import-statement
   wrapped-interpolated-string
   wrapped-float-lit
   wrapped-bool-lit
   else
   wrapped-nil-lit
   array
   object
   (<|> wrapped-identifier no-args-lambda-param)
   (<:> no-args-lambda)
   (<:> lambda)
   (<:> (parens (fwd expr)))
   (parens function-arguments)))

; See: http://www.difranco.net/compsci/C_Operator_Precedence_Table.htm
; Concat group needs to be higher than add group because
; it shares the '+' token

(def fun-group (chainl1 nweave fun-ops))
(def member-access-group (chainl1 fun-group
                                  (<|> dot-op (<:> function-call) (<:> indexing))))
(def concat-group (chainl1 member-access-group concat-op))
(def unary-group (prefix1 concat-group wrapped-uni-op))
(def mul-group (chainl1 unary-group wrapped-mul-op))
(def add-group (chainl1 mul-group wrapped-add-op))
(def rel-group (chainl1 add-group wrapped-rel-op))
(def range-group (chainl1 rel-group
                          (<|> open-range-op closed-range-op)))
(def eq-group (chainl1 range-group wrapped-eq-op))
(def and-group (chainl1 eq-group wrapped-and-op))
(def xor-group (chainl1 and-group wrapped-xor-op))
(def or-group (chainl1 xor-group wrapped-or-op))
(def expr (postfix1 or-group match-scope))
