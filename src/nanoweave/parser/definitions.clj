(ns ^{:doc "The nanoweave transform parser.", :author "Sean Dawson"}
nanoweave.parser.definitions
  (:use [blancas.kern.core]
        [blancas.kern.expr]
        [blancas.kern.lexer.basic]
        [nanoweave.parser.ast]))

; -- Grammar --
; pair          ::=  string ':' (json | expr)
; array         ::=  '[' (json (',' json)*)* ']'
; object        ::=  '{' (pair (',' pair)*)* '}'
; json          ::=  string | number | object | array | true | false | null
; expr          ::= prop-access
; prop-access   ::= identifier ('.' identifier)*

; Wrappers to convert basic types into AST types

(def wrapped-identifier (>>= identifier (fn [v] (return (->IdentiferLit v)))))
(def wrapped-string-lit (>>= string-lit (fn [v] (return (->StringLit v)))))
(def wrapped-float-lit (>>= float-lit (fn [v] (return (->FloatLit v)))))
(def wrapped-bool-lit (>>= bool-lit (fn [v] (return (->BoolLit v)))))
(def wrapped-nil-lit (>>= nil-lit (fn [_] (return (->NilLit)))))
(def dot-op
  "Access operator: extract value from object."
  (bind [op (token ".")]
        (return ({"." ->DotOp} op))))
(def concat-op
  "Parses one of the relational operators."
  (bind [op (token "++")]
        (return ({"++" ->ConcatOp} op))))
(def wrapped-uni-op
  "Multiplicative operator: multiplication, division, or modulo."
  (bind [op (one-of "!-")]
        (return ({\! ->NotOp \- ->NegOp} op))))
(def wrapped-mul-op
  "Multiplicative operator: multiplication, division, or modulo."
  (bind [op (one-of "*/%")]
        (return ({\* ->MultOp \/ ->DivOp \% ->ModOp} op))))
(def wrapped-add-op
  "Additive operator: addition or subtraction."
  (bind [op (one-of "+-")]
        (return ({\+ ->AddOp \- ->SubOp} op))))
(def wrapped-rel-op
  "Relational operator: greater than etc."
  (bind [op (token ">=" "<=" ">" "<")]
        (return ({">=" ->GrThanEqOp "<=" ->LessThanEqOp ">" ->GrThanOp "<" ->LessThanOp} op))))
(def wrapped-eq-op
  "Equality operator: equal or not-equal"
  (bind [op (token "==" "!=")]
        (return ({"==" ->EqOp "!=" ->NotEqOp} op))))
(def wrapped-and-op
  "Logical AND operator"
  (bind [op (token "and")]
        (return ({"and" ->AndOp} op))))
(def wrapped-or-op
  "Logical OR operator"
  (bind [op (token "or")]
        (return ({"or" ->OrOp} op))))
(def wrapped-xor-op
  "Logical XOR operator"
  (bind [op (token "xor")]
        (return ({"xor" ->XorOp} op))))

(declare expr)

(def pair
  "Parses the rule:  pair := String ':' expr"
  (bind [f string-lit _ colon v expr] (return [f v])))

(def array
  "Parses the rule:  array := '[' (expr (',' expr)*)* ']'"
  (brackets (comma-sep (fwd expr))))

(def object
  "Parses the rule:  object := '{' (pair (',' pair)*)* '}'"
  (braces (bind [members (comma-sep pair)]
                (return (apply hash-map (reduce concat [] members))))))

(def nweave
  "Parses a nanoweave structure."
  (<|> wrapped-string-lit
       wrapped-float-lit
       wrapped-bool-lit
       wrapped-nil-lit
       array
       object
       wrapped-identifier
       (parens (fwd expr))))

; See: http://www.difranco.net/compsci/C_Operator_Precedence_Table.htm
; Concat group needs to be higher than add group because
; it shares the '+' token
(def member-selection-group (chainl1 nweave dot-op))
(def unary-group (prefix1 member-selection-group wrapped-uni-op))
(def concat-group (chainl1 unary-group concat-op))
(def mul-group (chainl1 concat-group wrapped-mul-op))
(def add-group (chainl1 mul-group wrapped-add-op))
(def rel-group (chainr1 add-group wrapped-rel-op))
(def eq-group (chainr1 rel-group wrapped-eq-op))
(def and-group (chainr1 eq-group wrapped-and-op))
(def xor-group (chainr1 and-group wrapped-xor-op))
(def expr (chainr1 xor-group wrapped-or-op))

