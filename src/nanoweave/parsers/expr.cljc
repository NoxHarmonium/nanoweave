(ns ^{:author "Sean Dawson"
      :doc "Parses a nanoweave transform expression.\n            An expression combines all the other parsers together and is recursive\n            to allow complex transforms to be parsed."}
 nanoweave.parsers.expr
  (:require [blancas.kern.core :refer [<:> <|> <?> fwd << eof]]
            [blancas.kern.expr :refer [postfix1 prefix1]]
            [blancas.kern.lexer.java-style :refer [parens]]
            [nanoweave.parsers.base :refer [chainl1*]]
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
             [make-function-arguments
              function-call
              make-lambda
              make-no-args-lambda
              no-args-lambda-param]]
            [nanoweave.parsers.literals
             :refer
             [wrapped-bool-lit
              wrapped-float-lit
              wrapped-identifier
              wrapped-nil-lit
              type-lit
              make-array-lit
              make-object-lit]]
            [nanoweave.parsers.pattern-matching :refer [make-match-scope]]
            [nanoweave.parsers.scope
             :refer
             [else import-statement indexing make-let-scope make-when-scope]]
            [nanoweave.parsers.text :refer [make-wrapped-interpolated-string regex]]
            [nanoweave.parsers.unary :refer [wrapped-uni-op]]))

; Forward declarations

(declare expr)

; These parsers recursively call `expr` and therefore would cause a cross namespace circular
; dependency. Turning them into factory parsers (with the -make prefix) allows us to get around this.
(def array-lit (make-array-lit (fn [s] (expr s))))
(def object-lit (make-object-lit (fn [s] (expr s))))
(def lambda (make-lambda (fn [s] (expr s))))
(def no-args-lambda (make-no-args-lambda (fn [s] (expr s))))
(def function-arguments (make-function-arguments (fn [s] (expr s))))
(def let-scope (make-let-scope (fn [s] (expr s))))
(def when-scope (make-when-scope (fn [s] (expr s))))
(def wrapped-interpolated-string (make-wrapped-interpolated-string (fn [s] (expr s))))
(def match-scope (make-match-scope (fn [s] (expr s))))

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
   array-lit
   object-lit
   (<|> wrapped-identifier no-args-lambda-param)
   (<:> no-args-lambda)
   (<:> lambda)
   (<:> (parens (fwd expr)))
   (parens function-arguments)))

; See: http://www.difranco.net/compsci/C_Operator_Precedence_Table.htm
; Concat group needs to be higher than add group because
; it shares the '+' token

(def fun-group (chainl1* nweave fun-ops))
(def member-access-group (chainl1* fun-group
                                   (<|> dot-op (<:> function-call) (<:> indexing))))
(def concat-group (chainl1* member-access-group concat-op))
(def unary-group (prefix1 concat-group wrapped-uni-op))
(def mul-group (chainl1* unary-group wrapped-mul-op))
(def add-group (chainl1* mul-group wrapped-add-op))
(def rel-group (chainl1* add-group wrapped-rel-op))
(def range-group (chainl1* rel-group
                           (<|> open-range-op closed-range-op)))
(def type-group (chainl1* range-group (<|> is-op as-op)))
(def eq-group (chainl1* type-group wrapped-eq-op))
(def and-group (chainl1* eq-group wrapped-and-op))
(def xor-group (chainl1* and-group wrapped-xor-op))
(def or-group (chainl1* xor-group wrapped-or-op))
(def expr (<?> (postfix1 or-group match-scope) "expression"))

(def single-expression (<< expr eof))
