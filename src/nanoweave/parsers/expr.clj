(ns ^{:author "Sean Dawson"
      :doc "Parses a nanoweave transform expression.\n            An expression combines all the other parsers together and is recursive\n            to allow complex transforms to be parsed."}
 nanoweave.parsers.expr
  (:require [blancas.kern.core :refer [<:> <|> fwd]]
            [blancas.kern.expr :refer [chainl1 postfix1 prefix1]]
            [blancas.kern.lexer.java-style :refer [parens]]
            [nanoweave.parsers.base :refer [array object]]
            [nanoweave.parsers.binary-arithmetic
             :refer
             [wrapped-add-op wrapped-mul-op]]
            [nanoweave.parsers.binary-functions :refer
             [filter-op map-op reduce-op regex-match-op regex-find-op regex-split-op]]
            [nanoweave.parsers.binary-logic
             :refer
             [wrapped-and-op
              wrapped-eq-op
              wrapped-or-op
              wrapped-rel-op
              wrapped-xor-op]]
            [nanoweave.parsers.binary-other
             :refer
             [closed-range-op concat-op dot-op open-range-op is-op as-op]]
            [nanoweave.parsers.lambda
             :refer
             [function-arguments
              function-call
              lambda
              no-args-lambda
              no-args-lambda-param]]
            [nanoweave.parsers.literals
             :refer
             [wrapped-bool-lit
              wrapped-float-lit
              wrapped-identifier
              wrapped-nil-lit
              type-lit]]
            [nanoweave.parsers.pattern-matching :refer [match-scope]]
            [nanoweave.parsers.scope
             :refer
             [else import-statement indexing let-scope when-scope]]
            [nanoweave.parsers.text :refer [wrapped-interpolated-string regex]]
            [nanoweave.parsers.unary :refer [wrapped-uni-op]]))

; Forward declarations

(declare expr)

(def fun-ops
  "Matches any of the functional binary operators (they have the same precedence)"
  (<|> map-op filter-op reduce-op regex-match-op regex-find-op regex-split-op))

; Root Definition

(def nweave
  "Parses a nanoweave structure."
  (<|>
   let-scope
   when-scope
   import-statement
   regex
   type-lit
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
                                  (<|> dot-op  (<:> function-call) (<:> indexing))))
(def concat-group (chainl1 member-access-group concat-op))
(def unary-group (prefix1 concat-group wrapped-uni-op))
(def mul-group (chainl1 unary-group wrapped-mul-op))
(def add-group (chainl1 mul-group wrapped-add-op))
(def rel-group (chainl1 add-group wrapped-rel-op))
(def range-group (chainl1 rel-group
                          (<|> open-range-op closed-range-op)))
(def type-group (chainl1 range-group (<|> is-op as-op)))
(def eq-group (chainl1 type-group wrapped-eq-op))
(def and-group (chainl1 eq-group wrapped-and-op))
(def xor-group (chainl1 and-group wrapped-xor-op))
(def or-group (chainl1 xor-group wrapped-or-op))
(def expr (postfix1 or-group match-scope))
