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

(def jvalue
  "Parses a JSON value."
  (<|> wrapped-string-lit
       wrapped-float-lit
       wrapped-bool-lit
       wrapped-nil-lit
       array
       object
       wrapped-identifier
       (parens (fwd expr))))
   

(def access (chainl1 jvalue dot-op))
(def unary (prefix1  access wrapped-uni-op))  ;;  -(10), !(3>0)
(def comb (chainl1 unary concat-op)) ;; "a" ++ "b"
(def term  (chainl1 comb wrapped-mul-op))  ;; 3 * 34 * ...
(def expr (chainl1 term wrapped-add-op)) ;; 5 + 2*3 + ...

